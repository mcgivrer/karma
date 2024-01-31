package my.karma.app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Main class for project Karma
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class KarmaPlatform extends JPanel implements KeyListener {

    private String title = "KarmaPlatform";
    private static final double MAX_VELOCITY = 32.0;
    private final ResourceBundle messages = ResourceBundle.getBundle("i18n.messages");

    private final int maxEntitiesInSpace = 2;
    private final int maxLevelsInSpace = 4;
    private boolean exit = false;

    private static int debug;
    private static String debugFilter = "";

    private final boolean[] keys = new boolean[1024];

    private JFrame frame;
    private BufferedImage buffer;
    private Dimension winSize;
    private Dimension resSize;
    private int strategyBufferNb;

    private long collisionCounter = 0;

    private final Configuration config;
    private World world;
    private Vector2D physicVelocityMax = new Vector2D(0, 0);
    private Vector2D physicAccelerationMax = new Vector2D(0, 0);


    private SceneManager sceneManager;
    private SpacePartition spacePartition;

    /**
     * Entity type for rendering purpose.
     */
    public enum EntityType {
        RECTANGLE,
        ELLIPSE,
        IMAGE
    }

    /**
     * Physic type for Entity behavior in physic engine calculation.
     */
    public enum PhysicType {
        /**
         * {@link Entity} has NO physic computation
         */
        NONE,
        /**
         * {@link Entity} is static, no move, but can interact with DYNAMIC entities.
         */
        STATIC,

        /**
         * {@link Entity} is DYNAMIC: it interacts with other {@link Entity} and can move.
         */
        DYNAMIC
    }

    /**
     * Mathematical Vector on two dimensions.
     * Will allow every calculus on an (x,y) vector tuple.
     */
    public static class Vector2D {
        public double x, y;

        public Vector2D(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Vector2D add(Vector2D other) {
            return new Vector2D(this.x + other.x, this.y + other.y);
        }

        public Vector2D subtract(Vector2D other) {
            return new Vector2D(this.x - other.x, this.y - other.y);
        }

        public Vector2D multiply(double scalar) {
            return new Vector2D(this.x * scalar, this.y * scalar);
        }

        public double dot(Vector2D other) {
            return this.x * other.x + this.y * other.y;
        }

        public double magnitude() {
            return Math.sqrt(x * x + y * y);
        }

        public Vector2D normalize() {
            double mag = magnitude();
            return new Vector2D(x / mag, y / mag);
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }

        public Vector2D addAll(List<Vector2D> forces) {
            Vector2D total = new Vector2D(0, 0);
            for (Vector2D f : forces) {
                total = total.add(f);
            }
            return total;
        }

        public Vector2D limit(Vector2D limitMax) {
            this.x = Math.signum(x) * Math.min(Math.abs(this.x), limitMax.x);
            this.y = Math.signum(y) * Math.min(Math.abs(this.y), limitMax.y);
            return this;
        }

        public double getDistance(Vector2D point) {
            return point.subtract(this).magnitude();
        }
    }

    /**
     * This {@link Configuration} class will manage all the properties and arguments to maintain configuration values
     * from the configuration file (config.properties), or from the java command line.
     */
    public static class Configuration {
        private final Properties config;
        private final KarmaPlatform app;

        public Configuration(KarmaPlatform app) {
            this.app = app;
            this.config = new Properties();
        }

        public void load(String path) {
            try {
                this.config.load(this.getClass().getResourceAsStream(path));
                List<String> propertyList = this.config.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.toList());
                parseArguments(propertyList);

            } catch (IOException e) {
                error("unable to read configuration file: %s", e.getMessage());
            }
        }

        public void parseCLI(String[] args) {
            List<String> lArgs = Arrays.asList(args);
            if (!lArgs.isEmpty()) {
                parseArguments(lArgs);
                lArgs.forEach(s -> info("- arg: %s", s));
            }
        }

        public void parseArguments(List<String> lArgs) {
            lArgs.forEach(s -> {
                String[] arg = s.split("=");
                switch (arg[0]) {
                    case "app.exit" -> app.exit = Boolean.parseBoolean(arg[1]);
                    case "app.debug" -> debug = Integer.parseInt(arg[1]);
                    case "app.debug.filter" -> debugFilter = arg[1];
                    case "app.title" -> app.title = arg[1];
                    case "app.window.size" -> {
                        String[] res = arg[1].split("x");
                        app.winSize = new Dimension(Integer.parseInt(res[0]), Integer.parseInt(res[1]));
                    }
                    case "app.rendering.buffer" -> {
                        String[] res = arg[1].split("x");
                        app.resSize = new Dimension(Integer.parseInt(res[0]), Integer.parseInt(res[1]));
                    }
                    case "app.rendering.strategy" -> app.strategyBufferNb = Integer.parseInt(arg[1]);
                    case "app.physic.world.play.area" -> {
                        String[] res = arg[1].split("x");
                        app.world = new World()
                                .setPlayArea(
                                        new Rectangle2D.Double(0, 0,
                                                Integer.parseInt(res[0]), Integer.parseInt(res[1])));
                    }
                    case "app.physic.world.gravity" -> {
                        app.world.setGravity(Double.parseDouble(arg[1]));
                    }
                    case "app.physic.velocity.max" -> {
                        String[] vals = arg[1].substring("(".length(), arg[1].length() - ")".length()).split(",");
                        app.physicVelocityMax = new Vector2D(Double.parseDouble(vals[0]), Double.parseDouble(vals[1]));
                    }
                    case "app.physic.acceleration.max" -> {
                        String[] vals = arg[1].substring("(".length(), arg[1].length() - ")".length()).split(",");
                        app.physicAccelerationMax = new Vector2D(Double.parseDouble(vals[0]), Double.parseDouble(vals[1]));
                    }
                    case "app.scenes.list" -> {
                        app.sceneManager = new SceneManager(app);
                        app.sceneManager.load(arg[1]);
                    }
                    case "app.scenes.default" -> app.sceneManager.setDefaultSceneName(arg[1]);

                    default -> error("Unknown %s attribute ", s);
                }
            });
        }
    }

    /**
     * The internal {@link Entity} managed by the {@link KarmaPlatform} platform itself.
     * All the {@link Entity} and its derived forms are contained by a {@link Scene} instance
     * and will be managed for a physical computation and rendering process.
     */
    public static class Entity {
        /*---- identification attributes ----*/
        /**
         * internal entity counters to feed the id.
         */
        private static long index = 0;
        long id = index++;
        public String name;

        /*---- Geometric attributes ----*/
        public Vector2D position = new Vector2D(0, 0);
        public double w, h;
        public Vector2D size = new Vector2D(0, 0);
        private Vector2D center;

        /*---- Physic computation attributes ----*/
        /**
         * Define if the Entity is active of not to be updated/drawn.
         * If duration!=-1 and life &gt; 0, the active boolean is true.
         */
        private boolean active = true;
        /**
         * Physic type (see {@link PhysicType} for definition). Physic behavior will be defined accordingly.
         */
        private PhysicType physicType = PhysicType.DYNAMIC;
        /**
         * Entity's velocity.
         */
        public Vector2D velocity = new Vector2D(0, 0);
        public Vector2D acceleration = new Vector2D(0, 0);
        public List<Vector2D> forces = new ArrayList<>();
        /**
         * {@link Material} characteristics apply for physic computation.
         */
        private Material material = Material.DEFAULT;
        /**
         * the {@link Entity}'s mass.
         */
        private double mass = 1.0;


        /*---- Rendering attributes ----*/
        private EntityType type = EntityType.RECTANGLE;
        /**
         * Filling color for {@link EntityType#RECTANGLE} or {@link EntityType#ELLIPSE}.
         */
        private Color fgColor = Color.WHITE;
        /**
         * Background Color for {@link EntityType#RECTANGLE} or {@link EntityType#ELLIPSE}.
         */
        private Color bgColor = Color.BLUE;
        /**
         * image to be used for {@link EntityType#IMAGE} typed {@link Entity}.
         */
        private BufferedImage image;
        /**
         * Rendering priority.
         */
        private int priority = 1;

        /*---- Collision and update attributes -----*/
        /**
         * Bounding box for this entity
         */
        public Rectangle2D box = new Rectangle2D.Double();
        /**
         * Duration for this {@link Entity}. If it is different from -1,
         * it defines the life duration in millisecond.
         */
        private double duration = -1;
        /**
         * Current life for this Entity.
         */
        private double life = 0;
        /**
         * If the flag isStatic set to true, the {@link Entity} is stick to the active {@link Camera}.
         */
        private boolean isStatic = false;
        /**
         * List of {@link Behavior} applied to this {@link Entity}.
         */
        private final java.util.List<Behavior<Entity>> behaviors = new ArrayList<>();
        /**
         * List of child {@link Entity}.
         */
        private final java.util.List<Entity> child = new ArrayList<>();
        /**
         * List of {@link CollisionEvent} link top this {@link Entity}.
         */
        private final Collection<CollisionEvent> collisions = new ArrayList<>();

        /**
         * Free List of attributes to be populated on needs according to {@link Entity}
         * usage and {@link Scene} interaction.
         */
        private final Map<String, Object> attributes = new ConcurrentHashMap<>();

        /**
         * Create a new {@link Entity} with a name.
         * The internal {@link Entity#id} for this {@link Entity} will be created based on the
         * internal {@link Entity#index} counter.
         *
         * @param name the name for this new Entity.
         */
        public Entity(String name) {
            this.name = name;
        }

        /**
         * Add a child {@link Entity} to this one.
         *
         * @param c the child {@link Entity} to be added.
         * @return this updated Entity (thanks to fluent API).
         */
        public Entity add(Entity c) {
            child.add(c);
            return this;
        }

        public Entity addForce(Vector2D f) {
            this.forces.add(f);
            return this;
        }

        /**
         * Add a new {@link CollisionEvent} to the {@link Entity}.
         *
         * @param ce the new {@link CollisionEvent} to be linked to this {@link Entity}.
         * @return this updated Entity (thanks to fluent API).
         */
        public Entity register(CollisionEvent ce) {
            collisions.add(ce);
            return this;
        }

        public Collection<CollisionEvent> getCollisions() {
            return collisions;
        }

        public Collection<Entity> getChild() {
            return child;
        }

        public void clearRegisteredCollisions() {
            this.collisions.clear();
        }

        /**
         * The {@link Entity} is updated with the elapsed time since the
         * previous call to set its new life and define status for active attribute.
         * update all the child {@link Entity}'s.
         *
         * @param d
         */
        public void update(double d) {
            updateBox();
            if (duration != -1 && isActive()) {
                life += d;
                if (life > duration) {
                    life = 0;
                    active = false;
                }
            }
            if (!child.isEmpty())
                child.stream().filter(Entity::isActive).forEach(c -> c.update(d));
        }

        /**
         * Define the Entity position.
         *
         * @param x the horizontal axis
         * @param y the vertical axis
         * @return this updated Entity (thanks to fluent API).
         */
        public Entity setPosition(double x, double y) {
            this.position = new Vector2D(x, y);
            updateBox();
            return this;
        }

        /**
         * Define the Entity position.
         *
         * @param p a Vector2D to define the new position.
         * @return this updated Entity (thanks to fluent API).
         */
        public Entity setPosition(Vector2D p) {
            this.position = p;
            updateBox();
            return this;
        }

        public Entity setAcceleration(double ax, double ay) {
            this.acceleration = new Vector2D(ax, ay);
            return this;
        }

        public Entity setAcceleration(Vector2D a) {
            this.acceleration = a;
            return this;
        }


        /**
         * Define the Entity velocity.
         *
         * @param dx the horizontal axis
         * @param dy the vertical axis
         * @return this updated Entity (thanks to fluent API).
         */
        public Entity setVelocity(double dx, double dy) {
            this.velocity = new Vector2D(dx, dy);
            return this;
        }

        /**
         * Define the Entity velocity.
         *
         * @param v a Vector2D to define the new velocity.
         * @return this updated Entity (thanks to fluent API).
         */
        public Entity setVelocity(Vector2D v) {
            this.velocity = v;
            return this;
        }

        /**
         * Define the Entity size.
         *
         * @param s a Vector2D to define the new size.
         * @return this updated Entity (thanks to fluent API).
         */
        public Entity setSize(Vector2D s) {
            this.w = s.x;
            this.h = s.y;
            return this;
        }

        public Entity setSize(double w, double h) {
            this.w = w;
            this.h = h;
            updateBox();
            return this;
        }

        /**
         * update the bounding box and the center attributes.
         */
        public void updateBox() {
            box.setFrame(position.x, position.y, w, h);
            center = position.add(new Vector2D((0.5 * w), (0.5 * h)));
        }

        public Entity setForegroundColor(Color frontColor) {
            this.fgColor = frontColor;
            return this;
        }

        public Entity setBackgroundColor(Color bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        public Entity setPriority(int p) {
            this.priority = p;
            return this;
        }

        public Entity setActive(boolean a) {
            this.active = a;
            return this;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isActive() {
            return active;
        }

        /**
         * Define the Entity {@link Material}.
         *
         * @param m a {@link Material} to define the new {@link Entity}'s material.
         * @return this updated Entity (thanks to fluent API).
         */
        public Entity setMaterial(Material m) {
            this.material = m;
            return this;
        }

        public Material getMaterial() {
            return material;
        }

        /**
         * Define the Entity image.
         *
         * @param img a BufferedImage to define the image.
         * @return this updated Entity (thanks to fluent API).
         */
        public Entity setImage(BufferedImage img) {
            this.image = img;
            return this;
        }

        public <T> Entity setAttribute(String attrName, T attrValue) {
            this.attributes.put(attrName, attrValue);
            return this;
        }

        public <T> T getAttribute(String attrName) {
            return (T) attributes.get(attrName);
        }

        public <T> T getAttributeOrDefault(String attrName, T defaultValue) {
            return (T) attributes.getOrDefault(attrName, defaultValue);
        }

        public Entity setType(EntityType entityType) {
            this.type = entityType;
            return this;
        }

        public EntityType getType() {
            return type;
        }

        public Entity addBehavior(Behavior<Entity> b) {
            this.behaviors.add(b);
            return this;
        }

        public List<Behavior<Entity>> getBehaviors() {
            return this.behaviors;
        }

        public Entity setPhysicType(PhysicType pt) {
            this.physicType = pt;
            return this;
        }

        public PhysicType getPhysicType() {
            return physicType;
        }

        public String toString() {
            return name + "[" + id + "]";
        }

        public void moveBy(double ix, double iy) {
            this.position = this.position.add(new Vector2D(ix, iy));
        }

        public Vector2D getVelocity() {
            return velocity;
        }

        public Vector2D getAcceleration() {
            return acceleration;
        }

        public Vector2D getPosition() {
            return position;
        }

        public RectangularShape getBounds() {
            return box.getBounds();
        }

        public double getMass() {
            return mass;
        }

        public Entity setMass(double m) {
            this.mass = m;
            return this;
        }

        public boolean isStatic() {
            return isStatic;
        }

        public Entity setStatic(boolean s) {
            this.isStatic = s;
            return this;
        }

        public Vector2D getCenter() {
            return this.center;
        }


        public BufferedImage getImage() {
            return image;
        }

        public Color getBackgroundColor() {
            return bgColor;
        }

        public Color getForegroundColor() {
            return fgColor;
        }

        public void resetForces() {
            forces.clear();
        }
    }

    /**
     * Define the Material characteristics to be used during the physic engine calculation.
     * Here are defined the density, the elasticity, and the friction factors.
     */
    public static class Material {
        public String name;
        public double friction;
        public double density;
        public double elasticity;

        /**
         * Default Material to set default factors to 1.0.
         */
        public static Material DEFAULT = new Material("DEFAULT", 1.0, 1.0, 0.0);

        /**
         * Create a new {@link Material}.
         *
         * @param name       the name for this new {@link Material}.
         * @param friction   the friction for this new {@link Material}.
         * @param density    the density for this new {@link Material}.
         * @param elasticity the elasticity for this new {@link Material}.
         */
        public Material(String name, double friction, double density, double elasticity) {
            this.name = name;
            this.friction = friction;
            this.density = density;
            this.elasticity = elasticity;
        }
    }

    public static class SpacePartition extends Rectangle2D.Double {
        private int maxObjectsPerNode = 10;
        private int maxTreeLevels = 5;

        private SpacePartition root;

        private final int level;
        private final java.util.List<Entity> objects;
        private final SpacePartition[] nodes;

        /**
         * Create a new {@link SpacePartition} with a depth level and its defined
         * rectangle area.
         *
         * @param pLevel  the depth level for this {@link SpacePartition}
         * @param pBounds the Rectangle area covered by this {@link SpacePartition}.
         */
        public SpacePartition(int pLevel, Rectangle pBounds) {
            level = pLevel;
            objects = new ArrayList<>();
            setRect(pBounds);
            nodes = new SpacePartition[4];
        }

        /**
         * Initialize the {@link SpacePartition} according to the defined configuration.
         * <p>
         * The configuration file will provide 2 parameters:
         * <ul>
         * <li><code>app.physic.space.max.entities</code> is the maximum number of
         * entities that a SpacePartition node can contain,</li>
         * <li><code>app.physic.space.max.levels</code> is the max Depth level the tree
         * hierarchy can contain.</li>
         * </ul>
         * </p>
         *
         * @param app the parent {@link KarmaPlatform} instance.
         */
        public SpacePartition(KarmaPlatform app) {
            this(0, app.world.getPlayArea().getBounds());
            this.maxObjectsPerNode = app.maxEntitiesInSpace;
            this.maxTreeLevels = app.maxLevelsInSpace;
        }

        /**
         * Clears the {@link SpacePartition} nodes.
         */
        public void clear() {
            objects.clear();
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] != null) {
                    nodes[i].clear();
                    nodes[i] = null;
                }
            }
        }

        /**
         * Split the current SpacePartition into 4 sub spaces.
         */
        private void split() {
            int subWidth = (int) (getWidth() / 2);
            int subHeight = (int) (getHeight() / 2);
            int x = (int) getX();
            int y = (int) getY();
            nodes[0] = new SpacePartition(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight));
            nodes[1] = new SpacePartition(level + 1, new Rectangle(x, y, subWidth, subHeight));
            nodes[2] = new SpacePartition(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight));
            nodes[3] = new SpacePartition(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight));
        }

        /**
         * Determine which {@link SpacePartition} node the {@link Entity} belongs to.
         *
         * @param pRect the {@link Entity} to search in the {@link SpacePartition}'s
         *              tree.
         * @return the depth level of the {@link Entity}; -1 means object cannot
         * completely fit
         * within a child node and is part of the parent node
         */
        private int getIndex(Entity pRect) {
            int index = -1;
            double verticalMidpoint = getX() + (getWidth() / 2);
            double horizontalMidpoint = getY() + (getHeight() / 2);
            // Object can completely fit within the top quadrants
            boolean topQuadrant = (pRect.position.getY() < horizontalMidpoint
                    && pRect.position.getY() + pRect.h < horizontalMidpoint);
            // Object can completely fit within the bottom quadrants
            boolean bottomQuadrant = (pRect.position.getY() > horizontalMidpoint);
            // Object can completely fit within the left quadrants
            if (pRect.position.getX() < verticalMidpoint && pRect.position.getX() + pRect.w < verticalMidpoint) {
                if (topQuadrant) {
                    index = 1;
                } else if (bottomQuadrant) {
                    index = 2;
                }
            }
            // Object can completely fit within the right quadrants
            else if (pRect.position.getX() > verticalMidpoint) {
                if (topQuadrant) {
                    index = 0;
                } else if (bottomQuadrant) {
                    index = 3;
                }
            }
            return index;
        }

        /**
         * Insert the {@link Entity} into the {@link SpacePartition} tree. If the node
         * exceeds the capacity, it will split and add all
         * objects to their corresponding nodes.
         *
         * @param pRect the {@link Entity} to insert into the tree.
         */
        public void insert(Entity pRect) {
            if (nodes[0] != null) {
                int index = getIndex(pRect);
                if (index != -1) {
                    nodes[index].insert(pRect);
                    return;
                }
            }
            objects.add(pRect);
            if (objects.size() > maxObjectsPerNode && level < maxTreeLevels) {
                if (nodes[0] == null) {
                    split();
                }
                int i = 0;
                while (i < objects.size()) {
                    int index = getIndex(objects.get(i));
                    if (index != -1) {
                        nodes[index].insert(objects.remove(i));
                    } else {
                        i++;
                    }
                }
            }
            // insert all children Entity
            pRect.getChild().forEach(this::insert);
        }

        /**
         * Find the {@link Entity} into the {@link SpacePartition} tree and return the
         * list of neighbour's entities.
         *
         * @param e the entity to find.
         * @return a list of neighbour's entities.
         */
        public List<Entity> find(Entity e) {
            List<Entity> list = new ArrayList<>();
            return find(list, e);
        }

        /*
         * Return all objects that could collide with the given object
         */
        private List<Entity> find(List<Entity> returnObjects, Entity pRect) {
            int index = getIndex(pRect);
            if (index != -1 && nodes[0] != null) {
                nodes[index].find(returnObjects, pRect);
            }
            returnObjects.addAll(objects);
            return returnObjects;
        }

        /**
         * Dispatch all the {@link Scene} {@link Entity}'s into the
         * {@link SpacePartition} tree.
         *
         * @param scene   the Scene to be processed.
         * @param elapsed the elapsed time since previous call (not used here).
         */
        public void update(Scene scene, double elapsed) {
            this.clear();
            scene.getEntities().forEach(this::insert);
        }

        public void initialize(KarmaPlatform app) {
            this.root = this;
        }

        /**
         * Draw all {@link SpacePartition} nodes with a following color code:
         * <ul>
         * <li><code>RED</code> the node is full,</li>
         * <li><code>ORANGE</code> the node has entities but is not full,</li>
         * <li><code>GREEN</code> the node is empty.</li>
         * </ul>
         *
         * @param g     the {@link Graphics2D} API instance
         * @param alpha the stroke size of the grid line.
         */
        public void draw(Graphics2D g, float alpha) {
            SpacePartition sp = this;
            if (objects.isEmpty()) {
                g.setColor(new Color(0.0f, 1.0f, 0.0f, alpha));
            } else if (objects.size() < maxObjectsPerNode) {
                g.setColor(new Color(1.0f, 1.0f, 0.0f, alpha));
            } else {
                g.setColor(new Color(1.0f, 0.0f, 0.0f, alpha));
            }
            g.setFont(g.getFont().deriveFont(9.0f));
            g.drawString("" + objects.size(), (int) x + 4, (int) y + 8);
            g.setStroke(new BasicStroke(0.5f));
            g.draw(this);
            if (this.nodes != null) {
                for (SpacePartition node : nodes) {
                    if (node != null) {
                        node.draw(g, alpha);
                    }
                }
            }
        }
    }

    public static class GridObject extends Entity {
        private int stepW = 16, stepH = 16;
        private float strokeSize = 1.0f;

        public GridObject(String name) {
            super(name);
        }

        public GridObject setGridStep(int stepW, int stepH) {
            this.stepW = stepW;
            this.stepH = stepH;
            return this;
        }

        public GridObject setStrokeSize(float s) {
            this.strokeSize = s;
            return this;
        }

    }

    public static class TextObject extends Entity {
        private String text;
        private String format = "";
        private Color textColor;
        private Font font;
        private Object value;

        public TextObject(String name) {
            super(name);
            setPhysicType(PhysicType.NONE);
            this.textColor = Color.WHITE;
            this.text = "EMPTY";
        }

        public TextObject setText(String t) {
            this.text = t;
            return this;
        }

        public String getText() {
            return text;
        }

        public TextObject setFormat(String f) {
            this.format = f;
            return this;
        }

        public String getFormat() {
            return format;
        }

        public TextObject setTextColor(Color tc) {
            this.textColor = tc;
            return this;
        }

        public Color getTextColor() {
            return this.textColor;
        }

        public TextObject setFont(Font f) {
            this.font = f;
            return this;
        }

        public Font getFont() {
            return font;
        }

        public TextObject setValue(Object v) {
            this.value = v;
            if (!this.format.isEmpty()) {
                this.text = String.format(this.format, value);
            }
            return this;
        }

        public Object getValue() {
            return value;
        }

        public void updateBox(double x, double y, double fontHeight, double textWidth) {
            this.box = new Rectangle2D.Double(x, y, fontHeight, textWidth);
        }
    }

    public interface Scene {
        String getTitle();

        Camera getCamera();

        void create(KarmaPlatform app);

        default void initialize(KarmaPlatform app) {
        }

        default void input(KarmaPlatform app) {

        }

        default void update(KarmaPlatform app, double d) {
        }

        default void draw(KarmaPlatform app, Graphics2D g) {
        }

        default void dispose(KarmaPlatform app) {
        }

        void addEntity(KarmaPlatform.Entity e);

        Collection<Entity> getEntities();

        Entity getEntity(String entityName);

        void clearEntities();

        default void onKeyReleased(KeyEvent ke) {
        }
    }

    public static class SceneManager {
        private final KarmaPlatform app;
        private Scene current;
        private final Map<String, Scene> scenes = new HashMap<>();
        private String defaultSceneName = "";

        public SceneManager(KarmaPlatform app) {
            this.app = app;
        }

        public void add(Scene scene) {
            this.scenes.put(scene.getTitle(), scene);
        }

        public void start() {
            if (Optional.ofNullable(current).isEmpty()) {
                start(defaultSceneName.isEmpty() ? "init" : defaultSceneName);
            }
        }

        public void start(String sceneName) {
            if (Optional.ofNullable(current).isEmpty() || !current.getTitle().equals(sceneName)) {
                this.current = scenes.get(sceneName);
            }
            this.current.clearEntities();
            this.current.create(app);
            this.current.initialize(app);
        }

        public void activate(String name) {
            if (Optional.ofNullable(this.current).isPresent()) {
                this.current.dispose(app);
            }
            start(name);
        }

        public Scene getCurrent() {
            return this.current;
        }

        public void load(String strList) {
            String[] sceneItems = strList.split(",");
            Arrays.stream(sceneItems).forEach(item -> {
                String[] attrs = item.split(":");
                // Create Scene instance according to the defined class.
                try {
                    Class<?> sceneClass = Class.forName(attrs[1]);
                    Constructor<?> cstr = sceneClass.getConstructor(KarmaPlatform.class);
                    Scene scene = (Scene) cstr.newInstance(this.app);
                    scenes.put(scene.getTitle(), scene);
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                         IllegalAccessException | InvocationTargetException e) {
                    error("Can not create the %s class: %s%n", attrs[1], e.getMessage());
                }
            });
        }

        public void setDefaultSceneName(String s) {
            this.defaultSceneName = s;
        }
    }

    public static class World {

        private Rectangle2D playArea;
        private double gravity;
        private final List<Disturbance> disturbances = new ArrayList<>();

        public World() {
            gravity = 0.981;
            playArea = new Rectangle2D.Double(0, 0, 1000, 1000);
        }

        public World setGravity(double g) {
            this.gravity = g;
            return this;
        }

        public World setPlayArea(Rectangle2D r) {
            this.playArea = r;
            return this;
        }

        double getGravity() {
            return gravity;
        }

        public Rectangle2D getPlayArea() {
            return playArea;
        }

        public World addPerturbation(Disturbance p) {
            disturbances.add(p);
            return this;
        }

        public List<Disturbance> getDisturbances() {
            return disturbances;
        }
    }

    public static class Disturbance extends Entity {

        /**
         * Create a new {@link Disturbance} with a name.
         * The internal {@link Entity#id} for this {@link Entity} will be created based on the
         * internal {@link Entity#index} counter.
         *
         * @param name the name for this new Entity.
         */
        public Disturbance(String name) {
            super(name);
            this.setPhysicType(PhysicType.NONE);
            this.setType(EntityType.RECTANGLE);
            setForegroundColor(null);
            setBackgroundColor(null);
        }
    }

    public interface Behavior<Entity> {
        default void onUpdate(KarmaPlatform a, Entity e, double d) {
        }

        default void onInput(KarmaPlatform a, Entity e) {
        }

        default void onDraw(KarmaPlatform a, Graphics2D g, Entity e) {
        }

        default void onCollision(CollisionEvent ce) {

        }
    }

    public static class Camera extends Entity {
        private Entity target;

        private Rectangle2D viewport;
        private double tween;

        public Camera(String name) {
            super(name);
        }

        public Camera setTarget(Entity target) {
            this.target = target;
            return this;
        }

        public Camera setViewport(Rectangle2D vp) {
            this.viewport = vp;
            return this;
        }

        public Camera setTweenFactor(double tf) {
            this.tween = tf;
            return this;
        }

        public Rectangle2D getViewport() {
            return this.viewport;
        }

        public Entity getTarget() {
            return this.target;
        }

        public double getTweenFactor() {
            return this.tween;
        }

        public void update(double dt) {
            this.position.x += Math
                    .ceil((target.position.x + (target.w * 0.5) - ((viewport.getWidth()) * 0.5) - this.position.x)
                            * tween * Math.min(dt, 10));
            this.position.y += Math
                    .ceil((target.position.y + (target.h * 0.5) - ((viewport.getHeight()) * 0.5) - this.position.y)
                            * tween * Math.min(dt, 10));

            this.viewport.setRect(this.position.x, this.position.y, this.viewport.getWidth(),
                    this.viewport.getHeight());
        }
    }

    public static class CollisionEvent {

        private final Entity srcCollision;
        private final Entity dstCollision;
        private Vector2D collisionNormal;
        private double penetrationDepth;
        private CollisionSide side;

        public CollisionEvent(Entity src, Entity dst) {
            srcCollision = src;
            dstCollision = dst;
        }

        public Entity getSrc() {
            return srcCollision;
        }

        ;

        public Entity getDst() {
            return dstCollision;
        }

        ;

        public CollisionEvent setNormal(Vector2D n) {
            collisionNormal = n;
            return this;
        }

        public Vector2D getNormal() {
            return collisionNormal;
        }

        public CollisionEvent setPenetrationDepth(double p) {
            penetrationDepth = p;
            return this;
        }

        public double getPenetrationDepth() {
            return penetrationDepth;
        }

        public CollisionEvent setCollisionSide(CollisionSide s) {
            side = s;
            return this;
        }

        public CollisionSide getSide() {
            return side;
        }

    }

    public enum CollisionSide {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT;
    }


    /**
     * ---- Where everything start ----
     */

    public KarmaPlatform() {
        info("Initialization karmaApp %s (%s)%n",
                messages.getString("app.name"),
                messages.getString("app.version"));
        config = new Configuration(this);
        config.load("/config.properties");
    }

    public void run(String[] args) {
        init(args);
        loop();
        dispose();
    }

    /**
     * Initialization of the Application by parsing the CLI arguments.
     *
     * @param args list of command line arguments.
     */
    private void init(String[] args) {
        // get configuration values.
        config.parseCLI(args);
        // Create window
        frame = new JFrame(String.format("%s (%s)",
                messages.getString("app.name"),
                messages.getString("app.version")));
        this.setPreferredSize(winSize);
        this.setMinimumSize(winSize);
        this.setMaximumSize(winSize);
        frame.setContentPane(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.createBufferStrategy(strategyBufferNb);
        frame.addKeyListener(this);
        frame.requestFocus();
        // Prepare drawing buffer.
        buffer = new BufferedImage(resSize.width, resSize.height, BufferedImage.TYPE_4BYTE_ABGR);

        spacePartition = new SpacePartition(this);
    }


    /**
     * The main game loop where all the 3 steps for the game are executed:
     *
     * <ul>
     *     <li>manage <code>input()</code>,</li>
     *     <li>process <code>update()</code>,</li>
     *     <li>render everything with  <code>draw()</code>.</li>
     * </ul>
     */
    private void loop() {
        sceneManager.start("title");
        long current = System.currentTimeMillis();
        long previous = current;
        double delta = 0;
        while (!exit) {
            current = System.currentTimeMillis();
            delta = current - previous;
            input();
            update(delta);
            draw();
            previous = current;
        }
    }

    /**
     * Manage all the user inputs declared into the active {@link Scene} instance,
     * and all the {@link Behavior}'s on {@link Scene}'s {@link Entity}.
     */
    public void input() {
        sceneManager.getCurrent().input(this);
        // process all input behaviors
        sceneManager.getCurrent().getEntities().stream()
                .filter(KarmaPlatform.Entity::isActive)
                .forEach(this::processInput);
    }

    /**
     * Process the specific input for an {@link Entity}
     *
     * @param e the Entity to process <code>onInput()</code> form its {@link Behavior}.
     */
    public void processInput(Entity e) {
        // apply entity's behaviors
        if (!e.getBehaviors().isEmpty()) {
            e.getBehaviors().forEach(b -> {
                b.onInput(this, e);
            });
        }
        // apply child's behaviors.
        e.getChild().forEach(this::processInput);
    }

    /**
     * Process everything about game mechanics at
     * <ul>
     *     <li>physic engine level,</li>
     *     <li>at {@link Scene} Level,</li>
     *     <li>and at {@link Entity}'s level.</li>
     * </ul>
     *
     * @param d ths is the elapsed time since the previous call.
     */
    public void update(double d) {

        Collection<Entity> entities = sceneManager.getCurrent().getEntities();
        cullingProcess(this, d);
        entities.stream()
                .filter(Entity::isActive)
                .forEach(e -> {
                    if (!e.getPhysicType().equals(PhysicType.NONE)) {

                        // if concerned, apply World disturbances.
                        applyWorldDisturbance(world, e, d);
                        // compute physic on the Entity (velocity & position)
                        applyPhysics(world, e, d);
                        // detect collision and apply response
                        detectCollision(world, e, d);
                        // update the entity (lifetime and active status)
                        e.update(d);
                        // update the bounding box for that entity
                        e.updateBox();
                    }
                });
        sceneManager.getCurrent().update(this, d);
        Camera cam = sceneManager.getCurrent().getCamera();
        if (Optional.ofNullable(cam).isPresent()) {
            cam.update(d);
        }
    }

    /**
     * Apply the physic mechanics from the physic engine on the specified {@link Entity} instance,
     * applying the {@link World} context.
     *
     * @param world  the World object depicting the environment context.
     * @param entity the Entity to be processed
     * @param d      the elapsed tie since the previous call.
     */
    private void applyPhysics(World world, Entity entity, double d) {
        // apply velocity computation
        if (entity.getPhysicType().equals(PhysicType.DYNAMIC)) {

            // compute acceleration for this Entity
            entity.acceleration = entity.acceleration
                    .addAll(entity.forces)
                    .limit(physicAccelerationMax);

            // Compute velocity based on acceleration of this Entity
            entity.velocity = entity.velocity
                    .add(new Vector2D(0, (world.getGravity() * 0.01)))
                    .add(entity.acceleration.multiply(d))
                    .limit(physicVelocityMax);

            // Compute position according to velocity
            entity.position = entity.position.add(entity.getVelocity().multiply(d));

            // Update the bounding box.
            entity.updateBox();
            // apply possible behavior#update
            if (!entity.getBehaviors().isEmpty()) {
                entity.getBehaviors().forEach(b -> {
                    b.onUpdate(this, entity, d);
                    entity.updateBox();
                });
            }
            // keep entity in the KarmaApp area
            keepInPlayArea(world, entity);
            // update the box for the entity.
            entity.updateBox();
            // apply physic computation on children (if any)
            entity.getChild().stream().filter(Entity::isActive).forEach(c -> {
                applyPhysics(world, c, d);
                detectCollision(world, c, d);
            });
            entity.resetForces();
        }
    }

    private void applyWorldDisturbance(World world, Entity entity, double d) {
        for (Disturbance dist : world.disturbances) {
            if (dist.box.intersects(entity.box) || dist.box.contains(entity.box)) {
                // TODO add forces and acceleration to Entity.
                entity.forces.addAll(dist.forces);
            }
        }
    }

    /**
     * After physic processing, keep entity into the game area.
     *
     * @param w the World object depicting the environment context.
     * @param e the Entity to be processed
     */
    private void keepInPlayArea(World w, Entity e) {
        if (e.getPhysicType().equals(PhysicType.DYNAMIC)) {
            Rectangle2D playArea = w.getPlayArea();
            if (!playArea.contains(e.box)) {
                double elasticity = Math.min(e.getMaterial().elasticity, 1.0);
                if (e.position.x < playArea.getX()) {
                    e.velocity.x = e.velocity.x * -elasticity;
                    e.position.x = playArea.getX();
                }
                if (e.position.y < playArea.getY()) {
                    e.velocity.y = e.velocity.y * -elasticity;
                    e.position.y = playArea.getY();
                }
                if (e.position.x + e.w > playArea.getX() + playArea.getWidth()) {
                    e.velocity.x = e.velocity.x * -elasticity;
                    e.position.x = playArea.getX() + playArea.getWidth() - e.w;
                }
                if (e.position.y + e.h > playArea.getY() + playArea.getHeight()) {
                    e.velocity.y = e.velocity.y * -elasticity;
                    e.position.y = playArea.getY() + playArea.getHeight() - e.h;
                }
                e.updateBox();
            }
        }
    }

    /**
     * When all the physic and position are computed, detect possible collision,
     * and request to compute the corresponding response.
     *
     * @param w the World object depicting the environment context.
     * @param e the Entity to be processed
     * @param d the elapsed tie since the previous call.
     */
    private void detectCollision(World w, Entity e, double d) {
        Collection<Entity> entities = sceneManager.getCurrent().getEntities();
        // TODO: broad phase detect Entity at proximity cell through a Quadtree
        List<Entity> collisionList = new CopyOnWriteArrayList<>();
        spacePartition.find(collisionList, e);
        collisionCounter = 0;
        e.clearRegisteredCollisions();
        collisionList.forEach(o -> {
            if (e.isActive() && !o.equals(e) && o.isActive()
                    && !o.getPhysicType().equals(PhysicType.NONE)) {
                collisionCounter++;
                handleCollision(e, o);
            }
        });
    }

    /**
     * Dispatch all the active entities into the Space partitioning system to reduce collision
     * detections and optimize processing.
     *
     * @param game the parent game instance
     * @param d    the elapsed time since previous call.
     */
    public synchronized void cullingProcess(KarmaPlatform game, double d) {
        spacePartition.clear();
        sceneManager.getCurrent().getEntities().stream()
                .filter(Entity::isActive)
                .forEach(e -> {
                    spacePartition.insert(e);
                });
    }

    /**
     * Process the collision response by creating the corresponding {@link CollisionEvent},
     * and set the right {@link CollisionSide}.
     *
     * @param e the {@link Entity} source of collision
     * @param o the {@link Entity}'s colliding with.
     */
    private void handleCollision(Entity e, Entity o) {
        if (e.box.intersects(o.box)) {
            // Detect Collision Side
            CollisionEvent ce = new CollisionEvent(e, o);
            ce.setNormal(calculateCollisionNormal(e, o));
            ce.setPenetrationDepth(calculatePenetrationDepth(e, o, ce.getNormal()));
            if (Math.abs(ce.getNormal().y) > Math.abs(ce.getNormal().x)) {
                if (ce.getNormal().x < 0) {
                    ce.setCollisionSide(CollisionSide.RIGHT);
                } else {
                    ce.setCollisionSide(CollisionSide.LEFT);
                }
            } else {
                if (ce.getNormal().y > 0) {
                    ce.setCollisionSide(CollisionSide.BOTTOM);
                } else {
                    ce.setCollisionSide(CollisionSide.TOP);
                }
            }
            e.getBehaviors().forEach(b -> b.onCollision(ce));
            resolveCollision(ce);
            e.register(ce);
            o.getChild().forEach(c -> handleCollision(e, c));
            e.updateBox();
            if (debugFilter.contains(e.name) || debugFilter.isEmpty()) {
                debug("handle collision on %s between '%s' and '%s'", ce.side, ce.getSrc(), ce.getDst());
            }
        }
    }

    /**
     * Compute the collision response upon the {@link CollisionEvent}.
     *
     * @param ce the {@link CollisionEvent} to be processed.
     */
    private void resolveCollision(CollisionEvent ce) {
        // Déterminer le vecteur normal de la collision
        Vector2D normal = calculateCollisionNormal(ce.getSrc(), ce.getDst());

        // Résoudre la collision en fonction du vecteur normal, de l'élasticité et de la
        // friction
        Material material1 = ce.getSrc().getMaterial();
        Material material2 = ce.getDst().getMaterial();
        double elasticity = material1.elasticity * material2.elasticity;

        // Calculer les vitesses de collision en tenant compte des masses
        Vector2D v1 = ce.getSrc().getVelocity();
        Vector2D v2 = ce.getDst().getVelocity();
        double m1 = ce.getSrc().getMass();
        double m2 = ce.getDst().getMass();

        // Calculer la vitesse relative
        Vector2D relativeVelocity = v2.subtract(v1);
        double velocityAlongNormal = relativeVelocity.dot(normal);

        // Calculer l'impulsion scalaire
        double j = -(1 + elasticity) * velocityAlongNormal;
        j /= (1 / m1) + (1 / m2);

        // Appliquer l'impulsion aux entités
        Vector2D impulse = normal.multiply(j);
        if (ce.getSrc().getPhysicType().equals(PhysicType.DYNAMIC)) {
            ce.getSrc().setVelocity(v1.subtract(impulse.multiply(1 / m1)));
            limitVelocity(ce.getSrc());
        }
        if (ce.getDst().getPhysicType().equals(PhysicType.DYNAMIC)) {
            ce.getDst().setVelocity(v2.add(impulse.multiply(1 / m2)));
            limitVelocity(ce.getDst());
        }

        // ne fonctionne pas avec le contact des object static...
        // Calcul de la pénétration
        double penetrationDepth = calculatePenetrationDepth(ce.getSrc(), ce.getDst(), normal);
        if (penetrationDepth > 0) {
            Vector2D velocity1 = ce.getSrc().getVelocity();
            Vector2D velocity2 = ce.getDst().getVelocity();

            boolean isEntity1Dynamic = ce.getSrc().getPhysicType() == PhysicType.DYNAMIC;
            boolean isEntity2Dynamic = ce.getDst().getPhysicType() == PhysicType.DYNAMIC;
            boolean isEntity1Static = ce.getSrc().getPhysicType() == PhysicType.STATIC;
            boolean isEntity2Static = ce.getDst().getPhysicType() == PhysicType.STATIC;

            if (isEntity1Dynamic && isEntity2Static) {
                // Dynamic vs Static: Correction basée sur la plus grande composante de la
                // vitesse de l'entité dynamique
                applyPositionCorrection(ce.getSrc(), ce.getDst(), velocity1, normal);
                ce.getSrc().updateBox();
                ce.getSrc().getVelocity().y = ce.getDst().getVelocity().y * -ce.getDst().getMaterial().elasticity;
            } else if (isEntity1Static && isEntity2Dynamic) {
                // Static vs Dynamic: Correction basée sur la plus grande composante de la
                // vitesse de l'entité dynamique
                applyPositionCorrection(ce.getDst(), ce.getSrc(), velocity2, normal);
                ce.getDst().updateBox();
                ce.getSrc().getVelocity().y = ce.getDst().getVelocity().y * -ce.getDst().getMaterial().elasticity;
            } else if (isEntity1Dynamic && isEntity2Dynamic) {
                // Dynamic vs Dynamic: Correction partagée
                double totalMass = ce.getSrc().getMass() + ce.getDst().getMass();
                ce.getSrc().setPosition(
                        ce.getSrc().getPosition().add(normal.multiply(penetrationDepth * (ce.getDst().getMass() / totalMass))));
                ce.getDst().setPosition(ce.getDst().getPosition()
                        .subtract(normal.multiply(penetrationDepth * (ce.getSrc().getMass() / totalMass))));
                ce.getSrc().updateBox();
                ce.getDst().updateBox();
            }
        }
    }

    /**
     * Fixing the source dynamic {@link Entity} position according to the destination static {@link Entity}.
     *
     * @param dynEntity  the {@link PhysicType#DYNAMIC} impacted {@link Entity}.
     * @param statEntity the {@link PhysicType#STATIC} {@link Entity} to apply repositioning from.
     * @param velocity   velocity of the Dynamic Entity
     * @param normal     the resulting normal of the collision.
     */
    private void applyPositionCorrection(Entity dynEntity,
                                         Entity statEntity,
                                         Vector2D velocity,
                                         Vector2D normal) {
        double sideThreshold = 4;
        // Calculer la profondeur de la pénétration
        double overlapX = Math.min(dynEntity.box.getMaxX(), statEntity.box.getMaxX())
                - Math.max(dynEntity.box.getMinX(), statEntity.box.getMinX());
        double overlapY = Math.min(dynEntity.box.getMaxY(), statEntity.box.getMaxY())
                - Math.max(dynEntity.box.getMinY(), statEntity.box.getMinY());

        // Calculer la direction de la collision
        double velocityX = statEntity.box.getCenterX() - dynEntity.box.getCenterX();
        double velocityY = statEntity.box.getCenterY() - dynEntity.box.getCenterY();

        // Réajuster les positions des rectangles pour les séparer
        if (Math.abs(overlapX) < Math.abs(overlapY)) {
            if (velocityX > 0) {
                dynEntity.getPosition().x = (statEntity.box.getX() - dynEntity.box.getWidth());
            } else {
                dynEntity.getPosition().x = (statEntity.box.getX() + statEntity.box.getWidth());
            }
        } else {
            if (velocityY > 0) {
                dynEntity.getPosition().y = (statEntity.box.getY() - dynEntity.box.getHeight());
            } else {
                dynEntity.getPosition().y = (statEntity.box.getY() + statEntity.box.getHeight());
            }
        }
        dynEntity.updateBox();
    }

    /**
     * Compute the penetration factor resulting from the collision between entity1 and entity2,
     * with the resulting collision normal.
     *
     * @param entity1 the collision source {@link Entity}
     * @param entity2 the collision destination {@link Entity}
     * @param normal  the resulting normal for the collision between entity1 and entity2.
     * @return the computed penetration vector along the collision normal.
     */
    private double calculatePenetrationDepth(Entity entity1, Entity entity2, Vector2D normal) {

        // Calcul de la pénétration sur l'axe X
        double penetrationDepthX = 0;
        if (normal.x != 0) {
            if (normal.x > 0) {
                penetrationDepthX = (entity1.getBounds().getMaxX() - entity2.getBounds().getMinX());
            } else {
                penetrationDepthX = (entity2.getBounds().getMaxX() - entity1.getBounds().getMinX());
            }
        }

        // Calcul de la pénétration sur l'axe Y
        double penetrationDepthY = 0;
        if (normal.y != 0) {
            if (normal.y > 0) {
                penetrationDepthY = (entity1.getBounds().getMaxY() - entity2.getBounds().getMinY());
            } else {
                penetrationDepthY = (entity2.getBounds().getMaxY() - entity1.getBounds().getMinY());
            }
        }

        // Retourner la pénétration selon la direction de la normale
        return normal.x != 0 ? penetrationDepthX : penetrationDepthY;
    }

    /**
     * Limit the resulting velocity on {@link Entity} the max value of {@link KarmaPlatform#MAX_VELOCITY}.
     */
    private void limitVelocity(Entity entity) {
        Vector2D velocity = entity.getVelocity();
        if (velocity.magnitude() > physicVelocityMax.magnitude()) {
            entity.setVelocity(velocity.normalize().multiply(physicVelocityMax.magnitude()));
        }
    }

    /**
     * Compute the collision Normal between entity1 and entity2.
     *
     * @param entity1 the collision source {@link Entity}
     * @param entity2 the collision destination {@link Entity}
     * @return the resulting collision normal.
     */
    private Vector2D calculateCollisionNormal(Entity entity1, Entity entity2) {
        // Calculer un vecteur normal simplifié basé sur la position des entités
        Vector2D toEntity2 = entity2.getCenter().subtract(entity1.getCenter());
        return toEntity2.normalize();
    }

    /*---- Rendering process ----*/

    /**
     * Drawing all the game graphics onto the screen buffer,
     * and then copy this buffer to the window.
     */
    public void draw() {
        // prepare rendering pipeline
        Graphics2D g = buffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Clear display area
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        Scene scene = sceneManager.getCurrent();
        Camera cam = sceneManager.getCurrent().getCamera();

        // Draw things
        Collection<Entity> entities = scene.getEntities();
        entities.stream()
                .filter(Entity::isActive)
                .sorted(Comparator.comparingInt(Entity::getPriority))
                .forEach(e -> {
                    if (Optional.ofNullable(cam).isPresent() && !e.isStatic()) {
                        g.translate(
                                -cam.position.getX(),
                                -cam.position.getY());
                    }
                    draw(g, e);
                    e.getChild().forEach(c -> draw(g, c));
                    if (Optional.ofNullable(cam).isPresent() && !e.isStatic()) {
                        g.translate(
                                cam.position.getX(),
                                cam.position.getY());
                    }
                });
        sceneManager.getCurrent().draw(this, g);

        if (isDebugGreaterThan(3)) {
            if (Optional.ofNullable(cam).isPresent()) {
                g.translate(
                        -cam.position.getX(),
                        -cam.position.getY());
            }
            spacePartition.draw(g, 0.5f);
            if (Optional.ofNullable(cam).isPresent()) {
                g.translate(
                        cam.position.getX(),
                        cam.position.getY());
            }
        }
        // free API
        g.dispose();

        // Copy buffer to window.
        BufferStrategy bs = frame.getBufferStrategy();
        // configure renderer for antialiasing.
        Graphics2D gs = (Graphics2D) bs.getDrawGraphics();
        gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gs.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gs.drawImage(buffer,
                0, 32, winSize.width + 16, winSize.height + 32,
                0, 0, buffer.getWidth(), buffer.getHeight(),
                null);

        if (isDebugGreaterThan(1)) {
            displayDebugLineOnScreen(gs, entities);
        }

        // Switch buffer strategy
        bs.show();
        // free API
        gs.dispose();
    }

    /**
     * Display the debug information line onto the window bottom.
     *
     * @param gs       the {@link Graphics2D} API to use to draw onto the target window.
     * @param entities the list fo entities to extract information from.
     */
    private void displayDebugLineOnScreen(Graphics2D gs, Collection<Entity> entities) {
        long countActiveEntities = entities.stream()
                .filter(Entity::isActive).count();
        long countStaticEntities = entities.stream()
                .filter(e -> e.getPhysicType().equals(PhysicType.STATIC)).count();
        long countDynamicEntities = entities.stream()
                .filter(e -> e.getPhysicType().equals(PhysicType.DYNAMIC)).count();
        long countNoneEntities = entities.stream()
                .filter(e -> e.getPhysicType().equals(PhysicType.NONE)).count();
        long collidingEventsCount = collisionCounter;
        gs.setColor(new Color(0.6f, 0.3f, 0.1f, 0.50f));
        gs.fillRect(8, winSize.height + 8, winSize.width, 32);
        gs.setColor(Color.ORANGE);
        gs.drawString(
                String.format("[ debug: %d | entity(sta:%d,dyn:%d,non:%d) | active:%d | collision:%d ]",
                        debug,
                        countStaticEntities,
                        countDynamicEntities,
                        countNoneEntities,
                        countActiveEntities,
                        collidingEventsCount),
                16, winSize.height + 24);
    }

    /**
     * Draw an {@link Entity} according to its own nature and delegate rendering to specialized method.
     *
     * @param g the Graphics2D API instance to use.
     * @param e the Entity to be drawn
     */
    private void draw(Graphics2D g, Entity e) {
        switch (e.getClass().getSimpleName()) {
            case "TextObject" -> {
                drawTextObject(g, (TextObject) e);
            }
            case "Entity" -> {
                drawEntity(g, e);
            }
            case "GridObject" -> {
                drawGridObject(g, (GridObject) e);
            }
        }
        if (!e.getBehaviors().isEmpty()) {
            e.getBehaviors().forEach(b -> {
                b.onDraw(this, g, e);
            });
        }
        // drawing some debug information.
        if (isDebugGreaterThan(1)) {
            g.setColor(Color.ORANGE);
            g.setFont(g.getFont().deriveFont(9.0f));
            g.drawString("#" + e.id + "=" + e.name, (int) e.getPosition().getX() - 2, (int) e.getPosition().getY() - 2);
            g.setStroke(new BasicStroke(0.5f));
            if (isDebugGreaterThan(2)) {
                g.draw(e.box);
            }
            // draw Velocity
            g.setColor(Color.CYAN);
            Vector2D pos1 = e.getPosition().add(e.getVelocity().multiply(100.0).add(new Vector2D(e.w, e.h).multiply(0.5)));
            g.drawLine(
                    (int) (e.getPosition().x + e.w * 0.5), (int) (e.getPosition().y + e.h * 0.5),
                    (int) pos1.getX(), (int) pos1.getY());
            if (isDebugGreaterThan(3)) {
                // draw collision normals
                g.setColor(Color.WHITE);
                e.getCollisions().forEach(ce -> {
                    Vector2D pos2 = ce.getSrc().getPosition()
                            .add(ce.getNormal()
                                    .multiply(10.0)
                                    .add(new Vector2D(e.w, e.h)
                                            .multiply(0.5)));
                    g.drawLine(
                            (int) (e.getPosition().x + e.w * 0.5), (int) (e.getPosition().y + e.h * 0.5),
                            (int) pos2.getX(), (int) pos2.getY());
                });
            }
            g.setStroke(new BasicStroke(1.0f));
        }
    }

    /**
     * draw a GridObject instance onto screen buffer.
     *
     * @param g  the {@link Graphics2D} API instance to use
     * @param go the {@link GridObject} to draw
     */
    private void drawGridObject(Graphics2D g, GridObject go) {
        // draw temporary background
        g.setColor(go.getForegroundColor());
        g.setStroke(new BasicStroke(go.strokeSize));
        for (double dx = 0; dx < world.getPlayArea().getWidth(); dx += go.stepW) {
            g.drawRect((int) dx, 0, 16, (int) world.getPlayArea().getHeight());
        }
        for (double dy = 0; dy < world.getPlayArea().getHeight(); dy += go.stepH) {
            g.drawRect(0, (int) dy, (int) world.getPlayArea().getWidth(), 16);
        }
        g.setStroke(new BasicStroke(1.0f));
    }

    /**
     * Draw a TextObject instance onto screen buffer.
     *
     * @param g  the {@link Graphics2D} API instance to use
     * @param to the {@link TextObject} to draw
     */
    private static void drawTextObject(Graphics2D g, TextObject to) {
        g.setFont(to.getFont());
        g.setColor(Color.BLACK);
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                g.drawString(to.getText(), (int) to.position.x + dx, (int) to.position.y + dy);
            }
        }
        g.setColor(to.getTextColor());
        g.drawString(to.getText(), (int) to.position.x, (int) to.position.y);
        FontMetrics fm = g.getFontMetrics();
        to.w = fm.stringWidth(to.getText());
        to.h = fm.getHeight();
        to.updateBox(to.getPosition().x, to.getPosition().y - to.h, to.w, to.h);
    }

    /**
     * Draw a default Entity onto the screen buffer.
     *
     * @param g the {@link Graphics2D} API instance to use
     * @param e the {@link Entity} to draw
     */
    private static void drawEntity(Graphics2D g, Entity e) {
        switch (e.type) {
            case RECTANGLE -> {
                if (Optional.of(e.getBackgroundColor()).isPresent()) {
                    g.setColor(e.getBackgroundColor());
                    g.fillRect((int) e.position.x, (int) e.position.y, (int) e.w, (int) e.h);
                }
                if (Optional.of(e.getForegroundColor()).isPresent()) {
                    g.setColor(e.getForegroundColor());
                    g.drawRect((int) e.position.x, (int) e.position.y, (int) e.w, (int) e.h);
                }
            }
            case ELLIPSE -> {
                if (Optional.of(e.getBackgroundColor()).isPresent()) {
                    g.setColor(e.getBackgroundColor());
                    g.fillOval((int) e.position.x, (int) e.position.y, (int) e.w, (int) e.h);
                }
                g.setColor(e.fgColor);
                g.drawOval((int) e.position.x, (int) e.position.y, (int) e.w, (int) e.h);
            }
            case IMAGE -> {
                // draw the Entity image with a direction set according to the Entity's velocity on the horizontal axis.
                g.drawImage(e.getImage(), (int) e.position.x, (int) e.position.y, (int) (e.velocity.x >= 0 ? e.w : -e.w), (int) e.h, null);
            }
        }
    }

    /**
     * Return true if level of debug is greater than the <code>dgt</code> value.
     *
     * @param dgt requested minimal level debug.
     * @return true if level of debug is greater than the <code>dgt</code> value
     */
    public static boolean isDebugGreaterThan(int dgt) {
        return debug > dgt;
    }

    /**
     * Freeing all the reserved resources.
     */
    private void dispose() {
        info("End of karmaApp Wind");
        if (Optional.ofNullable(sceneManager.getCurrent()).isPresent()) {
            sceneManager.getCurrent().dispose(this);
        }
        if (Optional.ofNullable(frame).isPresent()) {
            frame.dispose();
        }
    }

    /**
     * Log INFO level on console output.
     *
     * @param msg  the test message
     * @param args the required arguments.
     */
    public static void info(String msg, Object... args) {

        DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;
        System.out.printf("%s|INFO|%s%n", dtf.format(LocalDateTime.now()), String.format(msg, args));
    }

    /**
     * Log DEBUG level on console output.
     *
     * @param msg  the test message
     * @param args the required arguments.
     */
    public static void debug(String msg, Object... args) {
        DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;
        if (isDebugGreaterThan(1)) {
            System.out.printf("%s|DEBUG|%s%n", dtf.format(LocalDateTime.now()), String.format(msg, args));
        }
    }

    /**
     * Log ERROR level on console output.
     *
     * @param msg  the test message
     * @param args the required arguments.
     */
    public static void error(String msg, Object... args) {
        DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;
        System.out.printf("%s|ERROR|%s%n", dtf.format(LocalDateTime.now()), String.format(msg, args));
    }

    /*---- implementation fo the KeyListener interface ----*/

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
        switch (e.getKeyCode()) {
            // [ESCAPE] quit the demo
            case KeyEvent.VK_ESCAPE -> {
                this.exit = true;
            }
            // [CTRL]+[Z] reset the scene
            case KeyEvent.VK_Z -> {
                if (e.isControlDown()) {
                    sceneManager.getCurrent().clearEntities();
                    sceneManager.getCurrent().create(this);
                }
            }
            // [CTRL]+[R] Reshuffle speed on Entities "enemy_$"

            // [D] will switch debug level from off to 1-5.
            case KeyEvent.VK_D -> {
                // switch debug mode to next level (1->5) or switch off (0)
                debug = debug + 1 < 6 ? debug + 1 : 0;
            }
            default -> {
                // Nothing to do.
            }
        }
        sceneManager.getCurrent().onKeyReleased(e);
    }


    /**
     * Retrieve the {@link Graphics2D} API instance for  the current screen buffer.
     *
     * @return the {@link Graphics2D} instance.
     */
    public Graphics2D getGraphics() {
        return buffer.createGraphics();
    }

    /**
     * @return boolean return the exit
     */
    public boolean isExit() {
        return exit;
    }

    /**
     * @param exit the exit to set
     */
    public void setExit(boolean exit) {
        this.exit = exit;
    }

    /**
     * @return JFrame return the frame
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * @param buffer the buffer to set
     */
    public void setBuffer(BufferedImage buffer) {
        this.buffer = buffer;
    }

    /**
     * @return Dimension return the winSize
     */
    public Dimension getWinSize() {
        return winSize;
    }

    /**
     * @param winSize the winSize to set
     */
    public void setWinSize(Dimension winSize) {
        this.winSize = winSize;
    }

    /**
     * @return Dimension return the resSize
     */
    public Dimension getResSize() {
        return resSize;
    }

    /**
     * @param resSize the resSize to set
     */
    public void setResSize(Dimension resSize) {
        this.resSize = resSize;
    }

    /**
     * @return int return the strategyBufferNb
     */
    public int getStrategyBufferNb() {
        return strategyBufferNb;
    }

    /**
     * @param strategyBufferNb the strategyBufferNb to set
     */
    public void setStrategyBufferNb(int strategyBufferNb) {
        this.strategyBufferNb = strategyBufferNb;
    }

    /**
     * @return long return the collisionCounter
     */
    public long getCollisionCounter() {
        return collisionCounter;
    }

    /**
     * @param collisionCounter the collisionCounter to set
     */
    public void setCollisionCounter(long collisionCounter) {
        this.collisionCounter = collisionCounter;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param world the world to set
     */
    public void setWorld(World world) {
        this.world = world;
    }

    /**
     * @param sceneManager the sceneManager to set
     */
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    /**
     * @return SpacePartition return the spacePartition
     */
    public SpacePartition getSpacePartition() {
        return spacePartition;
    }

    /**
     * @param spacePartition the spacePartition to set
     */
    public void setSpacePartition(SpacePartition spacePartition) {
        this.spacePartition = spacePartition;
    }


    public Dimension getScreenSize() {
        return resSize;
    }

    public World getWorld() {
        return world;
    }

    public boolean isKeyPressed(int vkKeyCode) {
        return keys[vkKeyCode];
    }

    public String getMessage(String keyMsg) {
        return replaceTemplate(messages.getString(keyMsg), messages);
    }

    public static String replaceTemplate(String template, ResourceBundle values) {
        StringTokenizer tokenizer = new StringTokenizer(template, "${}", true);
        StringJoiner joiner = new StringJoiner("");

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            if (token.equals("$")) {
                if (tokenizer.hasMoreTokens()) {
                    token = tokenizer.nextToken();

                    if (token.equals("{") && tokenizer.hasMoreTokens()) {
                        String key = tokenizer.nextToken();

                        if (tokenizer.hasMoreTokens() && tokenizer.nextToken().equals("}")) {
                            String value = values.containsKey(key) ? values.getString(key) : "${" + key + "}";
                            joiner.add(value);
                        }
                    }
                }
            } else {
                joiner.add(token);
            }
        }

        return joiner.toString();
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public Configuration getConfiguration() {
        return config;
    }


    /**
     * The main method for the {@link KarmaPlatform} instance to start the game
     * with some possible command line arguments.
     *
     * @param argc the command line arguments array.
     */
    public static void main(String[] argc) {
        KarmaPlatform app = new KarmaPlatform();
        app.run(argc);
    }

}
