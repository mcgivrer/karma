package my.karma.app;

import my.karma.app.scenes.PlayScene;
import my.karma.app.scenes.TitleScene;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Main class for project Karma
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class KarmaApp extends JPanel implements KeyListener {

    private static final double MAX_VELOCITY = 10.0;
    private final ResourceBundle messages = ResourceBundle.getBundle("i18n.messages");
    private final Properties config = new Properties();
    private final int maxEntitiesInSpace = 2;
    private final int maxLevelsInSpace = 4;
    private boolean exit = false;
    private final boolean[] keys = new boolean[1024];
    private JFrame frame;
    private BufferedImage buffer;
    private Dimension winSize;
    private Dimension resSize;
    private int strategyBufferNb;

    private String title = "KarmaApp";
    private World world;
    private int debug;
    private SceneManager sceneManager;
    private SpacePartition spacePartition;

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
        return messages.getString(keyMsg);
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public enum EntityType {
        RECTANGLE,
        ELLIPSE
    }

    public enum PhysicType {
        NONE,
        STATIC,
        DYNAMIC
    }

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
    }


    public static class Entity {
        private static long index = 0;
        private PhysicType physicType = PhysicType.DYNAMIC;
        private boolean active = true;
        long id = index++;
        public String name;

        public Vector2D position = new Vector2D(0, 0);
        public Vector2D velocity = new Vector2D(0, 0);
        public int w, h;
        public Rectangle2D box = new Rectangle2D.Double();
        private Vector2D center;

        private Material material = Material.DEFAULT;
        private double mass = 1.0;

        public Color fc = Color.WHITE, bg = Color.BLUE;
        private int priority = 1;
        private final Map<String, Object> attributes = new ConcurrentHashMap<>();
        private EntityType type = EntityType.RECTANGLE;
        private List<Behavior<Entity>> behaviors = new ArrayList<>();

        private List<Entity> child = new ArrayList<>();
        private long duration = -1;
        private long life = 0;

        public Entity(String name) {
            this.name = name;
        }

        public Entity add(Entity c) {
            child.add(c);
            return this;
        }

        public Collection<Entity> getChild() {
            return child;
        }

        public void update(long d) {
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

        public Entity setPosition(double x, double y) {
            this.position = new Vector2D(x, y);
            updateBox();
            return this;
        }

        public Entity setPosition(Vector2D p) {
            this.position = p;
            updateBox();
            return this;
        }

        public Entity setVelocity(double dx, double dy) {
            this.velocity = new Vector2D(dx, dy);
            return this;
        }

        public Entity setVelocity(Vector2D v) {
            this.velocity = v;
            return this;
        }

        public Entity setSize(int w, int h) {
            this.w = w;
            this.h = h;
            updateBox();
            return this;
        }

        public void updateBox() {
            box.setFrame(position.x, position.y, w, h);
            center = position.add(new Vector2D((0.5 * w), (0.5 * h)));
        }

        public Entity setBorderColor(Color frontColor) {
            this.fc = frontColor;
            return this;
        }

        public Entity setBackgroundColor(Color bgColor) {
            this.bg = bgColor;
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

        public Entity setMaterial(Material m) {
            this.material = m;
            return this;
        }

        public Material getMaterial() {
            return material;
        }

        public <T> Entity addAttribute(String attrName, T attrValue) {
            this.attributes.put(attrName, attrValue);
            return this;
        }

        public <T> T getAttribute(String attrName) {
            return (T) attributes.get(attrName);
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
    }

    public static class Material {
        public double friction;
        public double density;
        public double elasticity;

        public static Material DEFAULT = new Material(1.0, 1.0, 0.0);

        public Material(double friction, double density, double elasticity) {
            this.friction = friction;
            this.density = density;
            this.elasticity = elasticity;
        }
    }

    public class SpacePartition extends Rectangle2D.Double {
        private int maxObjectsPerNode = 10;
        private int maxTreeLevels = 5;

        private SpacePartition root;

        private int level;
        private List<Entity> objects;
        private SpacePartition[] nodes;

        /**
         * Create a new {@link SpacePartition} with a depth level and its defined rectangle area.
         *
         * @param pLevel  the  depth level for this {@link SpacePartition}
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
         *     <ul>
         *         <li><code>app.physic.space.max.entities</code> is the maximum number of entities that a SpacePartition node can contain,</li>
         *         <li><code>app.physic.space.max.levels</code> is the max Depth level the tree hierarchy can contain.</li>
         *     </ul>
         * </p>
         *
         * @param app the parent {@link KarmaApp} instance.
         */
        public SpacePartition(KarmaApp app) {
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
         * @param pRect the {@link Entity} to search in the {@link SpacePartition}'s tree.
         * @return the depth level of the {@link Entity}; -1 means object cannot completely fit
         * within a child node and is part of the parent node
         */
        private int getIndex(Entity pRect) {
            int index = -1;
            double verticalMidpoint = getX() + (getWidth() / 2);
            double horizontalMidpoint = getY() + (getHeight() / 2);
            // Object can completely fit within the top quadrants
            boolean topQuadrant = (pRect.position.getY() < horizontalMidpoint && pRect.position.getY() + pRect.h < horizontalMidpoint);
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
            pRect.getChild().forEach(c -> insert(c));
        }

        /**
         * Find the {@link Entity} into the {@link SpacePartition} tree and return the list of neighbour's entities.
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
        private List find(List returnObjects, Entity pRect) {
            int index = getIndex(pRect);
            if (index != -1 && nodes[0] != null) {
                nodes[index].find(returnObjects, pRect);
            }
            returnObjects.addAll(objects);
            return returnObjects;
        }

        /**
         * Dispatch all the {@link Scene} {@link Entity}'s into the {@link SpacePartition} tree.
         *
         * @param scene   the Scene to be processed.
         * @param elapsed the elapsed time since previous call (not used here).
         */
        public void update(Scene scene, double elapsed) {
            this.clear();
            scene.getEntities().forEach(e -> this.insert(e));
        }


        public void initialize(KarmaApp app) {
            this.root = this;
        }


        /**
         * Draw all {@link SpacePartition} nodes with a following color code:
         * <ul>
         *     <li><code>RED</code> the node is full,</li>
         *     <li><code>ORANGE</code> the node has entities but is not full,</li>
         *     <li><code>GREEN</code> the node is empty.</li>
         * </ul>
         *
         * @param g     the {@link Graphics2D} API instance
         * @param scene the {@link Scene} to be processed.
         */
        public void draw(Graphics2D g, Scene scene) {
            SpacePartition sp = this;
            if (objects.isEmpty()) {
                g.setColor(Color.GREEN);
            } else if (objects.size() < maxObjectsPerNode) {
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(Color.RED);
            }
            g.setFont(g.getFont().deriveFont(9.0f));
            g.drawString("" + objects.size(), (int) x + 4, (int) y + 8);
            g.draw(this);
            if (this.nodes != null) {
                for (SpacePartition node : nodes) {
                    if (node != null) {
                        node.draw(g, scene);
                    }
                }
            }
        }
    }

    public static class GridObject extends Entity {
        private int stepW = 16, stepH = 16;

        public GridObject(String name) {
            super(name);
        }

        public GridObject setGridStep(int stepW, int stepH) {
            this.stepW = stepW;
            this.stepH = stepH;
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
            if (!this.format.equals("")) {
                this.text = String.format(this.format, value);
            }
            return this;
        }

        public Object getValue() {
            return value;
        }

    }

    public interface Scene {
        String getTitle();

        void create(KarmaApp app);

        default void initialize(KarmaApp app) {
        }

        default void input(KarmaApp app) {
        }

        default void update(KarmaApp app, long d) {
        }

        default void draw(KarmaApp app, Graphics2D g) {
        }

        default void dispose(KarmaApp app) {
        }

        void addEntity(KarmaApp.Entity e);

        Collection<Entity> getEntities();

        Entity getEntity(String entityName);

        void clearEntities();

        default void onKeyReleased(KeyEvent ke) {
        }
    }

    public static class SceneManager {
        private KarmaApp app;
        private Scene current;
        private Map<String, Scene> scenes = new HashMap<>();

        public SceneManager(KarmaApp app) {
            this.app = app;
        }

        public void add(Scene scene) {
            this.scenes.put(scene.getTitle(), scene);
        }

        public void start() {
            if (Optional.ofNullable(current).isEmpty()) {
                start("init");
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
    }

    public static class World {

        Rectangle2D playArea = new Rectangle2D.Double(0, 0, 1000, 1000);
        double gravity = 0.981;

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
    }

    public interface Behavior<Entity> {
        default void onUpdate(KarmaApp a, Entity e, long d) {
        }

        default void onInput(KarmaApp a, Entity e) {
        }

        default void onDraw(KarmaApp a, Graphics2D g, Entity e) {
        }

        default void onCollision(Entity src, Entity dst) {

        }
    }

    public KarmaApp() {
        info("Initialization karmaApp %s (%s)%n",
                messages.getString("app.name"),
                messages.getString("app.version"));
        loadConfiguration();
    }

    public void run(String[] args) {
        init(args);
        loop();
        dispose();
    }

    private void init(String[] args) {
        // get configuration values.
        parseCLI(args);
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
        sceneManager = new SceneManager(this);

        sceneManager.add(new TitleScene(this));
        sceneManager.add(new PlayScene(this));

        spacePartition = new SpacePartition(this);
    }

    private void loadConfiguration() {
        try {
            config.load(this.getClass().getResourceAsStream("/config.properties"));

            List<String> propertyList =
                    config.entrySet().stream()
                            .map(e -> String.valueOf(e.getKey()) + "=" + String.valueOf(e.getValue()))
                            .collect(Collectors.toList());
            parseArguments(propertyList);

        } catch (IOException e) {
            error("unable to read configuration file: %s", e.getMessage());
        }
    }

    private void parseCLI(String[] args) {
        List<String> lArgs = Arrays.asList(args);
        if (!lArgs.isEmpty()) {
            parseArguments(lArgs);
            lArgs.forEach(s -> {
                info("- arg: %s", s);
            });
        }
    }

    private void parseArguments(List<String> lArgs) {
        lArgs.forEach(s -> {
            String[] arg = s.split("=");
            switch (arg[0]) {
                case "app.exit" -> exit = Boolean.parseBoolean(arg[1]);
                case "app.debug" -> debug = Integer.parseInt(arg[1]);
                case "app.title" -> title = arg[1];
                case "app.window.size" -> {
                    String[] res = arg[1].split("x");
                    winSize = new Dimension(Integer.parseInt(res[0]), Integer.parseInt(res[1]));
                }
                case "app.rendering.buffer" -> {
                    String[] res = arg[1].split("x");
                    resSize = new Dimension(Integer.parseInt(res[0]), Integer.parseInt(res[1]));
                }
                case "app.rendering.strategy" -> strategyBufferNb = Integer.parseInt(arg[1]);
                case "app.physic.play.area" -> {
                    String[] res = arg[1].split("x");
                    world = new World()
                            .setPlayArea(new Rectangle2D.Double(0, 0,
                                    Integer.parseInt(res[0]), Integer.parseInt(res[1])));
                }
                case "app.physic.gravity" -> {
                    world.setGravity(Double.parseDouble(arg[1]));
                }
                case "app.scenes.list" -> {
                    String[] scenesStr = arg[1].split(",");
                    for (String sceneItem : scenesStr) {

                    }
                }
                case "app.scenes.default" -> {

                }

                default -> error("Unknown %s attribute ", s);
            }
        });
    }

    private void loop() {
        sceneManager.start("title");
        long current = System.currentTimeMillis();
        long previous = current;
        long delta = 0;
        while (!exit) {
            current = System.currentTimeMillis();
            delta = current - previous;
            input();
            update(delta);
            draw();
            previous = current;
        }
    }

    public void input() {
        sceneManager.getCurrent().input(this);
    }

    public void update(long d) {
        Collection<Entity> entities = sceneManager.getCurrent().getEntities();
        cullingProcess(this, d);
        entities.forEach(e -> {
            if (!e.getPhysicType().equals(PhysicType.NONE)) {

                applyPhysics(e, world, d);
                detectCollision(world, e, d);
                // update the entity (lifetime and active status)
                e.update(d);
                e.updateBox();
            }
        });
        sceneManager.getCurrent().update(this, d);
    }

    private void applyPhysics(Entity e, World w, long d) {
        // apply velocity computation
        if (e.getPhysicType().equals(PhysicType.DYNAMIC)) {
            // compute position according to velocity
            e.position = e.position.add(e.getVelocity().multiply(d))
                    .add(new Vector2D(0, (w.getGravity() * 0.1)).multiply(d));
            e.updateBox();
            // apply friction
            e.setVelocity(e.getVelocity().multiply(e.getMaterial().friction));
            // apply possible behavior#update
            if (!e.getBehaviors().isEmpty()) {
                e.getBehaviors().forEach(b -> {
                    b.onUpdate(this, e, d);
                    e.updateBox();
                });
            }
            // keep entity in the KarmaApp area
            keepInPlayArea(w, e);
            // update the box for the entity.
            e.updateBox();
            // apply physic computation on children (if any)
            e.getChild().stream().filter(Entity::isActive).forEach(c -> {
                applyPhysics(c, world, d);
                detectCollision(world, c, d);
            });
        }

    }

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

    private void detectCollision(World w, Entity e, double d) {
        Collection<Entity> entities = sceneManager.getCurrent().getEntities();
        //TODO: broad phase detect Entity at proximity cell through a Quadtree
        List<Entity> collisionList = new CopyOnWriteArrayList<>();
        spacePartition.find(collisionList, e);
        collisionList.forEach(o -> {
            if (e.isActive() && !o.equals(e) && o.isActive()
                    && !o.getPhysicType().equals(PhysicType.NONE)) {
                handleCollision(e, o);
            }
        });
    }

    public synchronized void cullingProcess(KarmaApp game, float dt) {
        spacePartition.clear();
        for (Entity e : sceneManager.getCurrent().getEntities()) {
            // inert object into QuadTree for collision detection.
            spacePartition.insert(e);
        }
    }

    private void handleCollision(Entity e, Entity o) {
        if (e.box.intersects(o.box)) { //isColliding(e, o)
            debug("collision between '%s' and '%s'", e, o);
            e.getBehaviors().forEach(b -> b.onCollision(e, o));
            resolveCollision(e, o);
            o.getChild().stream().forEach(c -> handleCollision(e, c));
            e.updateBox();
        }
    }

    private void resolveCollision(Entity entity1, Entity entity2) {
        // Déterminer le vecteur normal de la collision
        Vector2D normal = calculateCollisionNormal(entity1, entity2);

        // Résoudre la collision en fonction du vecteur normal, de l'élasticité et de la friction
        Material material1 = entity1.getMaterial();
        Material material2 = entity2.getMaterial();
        double elasticity = Math.min(material1.elasticity, material2.elasticity);

        // Calculer les vitesses de collision en tenant compte des masses
        Vector2D v1 = entity1.getVelocity();
        Vector2D v2 = entity2.getVelocity();
        double m1 = entity1.getMass();
        double m2 = entity2.getMass();

        // Calculer la vitesse relative
        Vector2D relativeVelocity = v2.subtract(v1);
        double velocityAlongNormal = relativeVelocity.dot(normal);

        // Ne pas résoudre la collision si les vitesses sont en train de s'éloigner l'une de l'autre
        //if (velocityAlongNormal > 0) {
        //    return;
        //}

        // Calculer l'impulsion scalaire
        double j = -(1 + elasticity) * velocityAlongNormal;
        j /= (1 / m1) + (1 / m2);

        // Appliquer l'impulsion aux entités
        Vector2D impulse = normal.multiply(j);
        if (entity1.getPhysicType().equals(PhysicType.DYNAMIC)) {
            entity1.setVelocity(v1.subtract(impulse.multiply(1 / m1)));
            limitVelocity(entity1);
        }
        if (entity2.getPhysicType().equals(PhysicType.DYNAMIC)) {
            entity2.setVelocity(v2.add(impulse.multiply(1 / m2)));
            limitVelocity(entity2);
        }

        // ne fonctionne pas avec le contact des object static...
        // Calcul de la pénétration
        double penetrationDepth = calculatePenetrationDepth(entity1, entity2, normal);
        if (penetrationDepth > 0) {
            Vector2D velocity1 = entity1.getVelocity();
            Vector2D velocity2 = entity2.getVelocity();

            boolean isEntity1Dynamic = entity1.getPhysicType() == PhysicType.DYNAMIC;
            boolean isEntity2Dynamic = entity2.getPhysicType() == PhysicType.DYNAMIC;
            boolean isEntity1Static = entity1.getPhysicType() == PhysicType.STATIC;
            boolean isEntity2Static = entity2.getPhysicType() == PhysicType.STATIC;

            if (isEntity1Dynamic && isEntity2Static) {
                // TODO: buggy pour la detection de plateforme
                // Dynamic vs Static: Correction basée sur la plus grande composante de la vitesse de l'entité dynamique
                applyPositionCorrection(entity1, entity2, velocity1, normal);
                entity1.updateBox();
                entity1.setVelocity(entity2.getVelocity().multiply(-entity1.getMaterial().elasticity));
            } else if (isEntity1Static && isEntity2Dynamic) {
                // TODO: buggy pour la detection de plateforme
                // Static vs Dynamic: Correction basée sur la plus grande composante de la vitesse de l'entité dynamique
                applyPositionCorrection(entity2, entity1, velocity2, normal);
                entity2.updateBox();
                entity2.setVelocity(entity1.getVelocity().multiply(-entity1.getMaterial().elasticity));
            } else if (isEntity1Dynamic && isEntity2Dynamic) {
                // Dynamic vs Dynamic: Correction partagée
                double totalMass = entity1.getMass() + entity2.getMass();
                entity1.setPosition(entity1.getPosition().add(normal.multiply(penetrationDepth * (entity2.getMass() / totalMass))));
                entity2.setPosition(entity2.getPosition().subtract(normal.multiply(penetrationDepth * (entity1.getMass() / totalMass))));
                entity1.updateBox();
                entity2.updateBox();
            }
        } else {
            debug("No collision between '%s' and '%s'", entity1, entity2);
        }
    }

    private void applyPositionCorrection(Entity dynEntity, Entity statEntity, Vector2D velocity, Vector2D normal) {
        double sideThreshold = 4;
        // Calculer la profondeur de la pénétration
        double overlapX = Math.min(dynEntity.box.getMaxX(), statEntity.box.getMaxX()) - Math.max(dynEntity.box.getMinX(), statEntity.box.getMinX());
        double overlapY = Math.min(dynEntity.box.getMaxY(), statEntity.box.getMaxY()) - Math.max(dynEntity.box.getMinY(), statEntity.box.getMinY());

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

    private void limitVelocity(Entity entity) {
        Vector2D velocity = entity.getVelocity();
        if (velocity.magnitude() > MAX_VELOCITY) {
            entity.setVelocity(velocity.normalize().multiply(MAX_VELOCITY));
        }
    }

    private Vector2D calculateCollisionNormal(Entity entity1, Entity entity2) {
        // Calculer un vecteur normal simplifié basé sur la position des entités
        Vector2D toEntity2 = entity2.getPosition().subtract(entity1.getPosition());
        return toEntity2.normalize();
    }

    public void draw() {
        // prepare rendering pipeline
        Graphics2D g = buffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Clear display area
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());

        // Draw things

        Collection<Entity> entities = sceneManager.getCurrent().getEntities();
        entities.stream()
                .filter(Entity::isActive)
                .sorted(Comparator.comparingInt(Entity::getPriority))
                .forEach(e -> {
                    draw(e, g);
                    e.getChild().forEach(c -> draw(c, g));
                });
        sceneManager.getCurrent().draw(this, g);
        spacePartition.draw(g, sceneManager.getCurrent());
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
            gs.setColor(new Color(0.6f, 0.6f, 0.6f, 0.50f));
            gs.fillRect(8, winSize.height + 8, winSize.width, 32);
            gs.setColor(Color.WHITE);
            gs.drawString(String.format("[dbg: %d | nb:%d]", debug, entities.size()), 16, winSize.height + 24);
        }
        // Switch buffer strategy
        bs.show();
        // free API
        gs.dispose();
    }

    private void draw(Entity e, Graphics2D g) {
        switch (e.getClass().getSimpleName()) {
            case "TextObject" -> {
                drawTextObject((TextObject) e, g);
            }
            case "Entity" -> {
                drawEntity(e, g);
            }
            case "GridObject" -> {
                drawGridObject((GridObject) e, g);
            }
        }
        if (!e.getBehaviors().isEmpty()) {
            e.getBehaviors().forEach(b -> {
                b.onDraw(this, g, e);
            });
        }
    }

    private void drawGridObject(GridObject go, Graphics2D g) {
        // draw temporary background
        g.setColor(go.fc);
        for (double dx = 0; dx < world.getPlayArea().getWidth(); dx += go.stepW) {
            g.drawRect((int) dx, 0, 16, (int) world.getPlayArea().getHeight());
        }
        for (double dy = 0; dy < world.getPlayArea().getHeight(); dy += go.stepH) {
            g.drawRect(0, (int) dy, (int) world.getPlayArea().getWidth(), 16);
        }
    }

    private static void drawTextObject(TextObject to, Graphics2D g) {
        g.setFont(to.getFont());
        g.setColor(Color.BLACK);
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                g.drawString(to.getText(), (int) to.position.x + dx, (int) to.position.y + dy);
            }
        }
        g.setColor(to.getTextColor());
        g.drawString(to.getText(), (int) to.position.x, (int) to.position.y);
    }

    private static void drawEntity(Entity e, Graphics2D g) {
        switch (e.type) {
            case RECTANGLE -> {
                g.setColor(e.bg);
                g.fillRect((int) e.position.x, (int) e.position.y, e.w, e.h);
                g.setColor(e.fc);
                g.drawRect((int) e.position.x, (int) e.position.y, e.w, e.h);
            }
            case ELLIPSE -> {
                g.setColor(e.bg);
                g.fillOval((int) e.position.x, (int) e.position.y, e.w, e.h);
                g.setColor(e.fc);
                g.drawOval((int) e.position.x, (int) e.position.y, e.w, e.h);
            }
        }
    }

    private boolean isDebugGreaterThan(int dgt) {
        return debug >= dgt;
    }

    private void dispose() {
        info("End of karmaApp Wind");
        if (Optional.ofNullable(frame).isPresent()) {
            frame.dispose();
        }
    }

    public static void info(String msg, Object... args) {
        System.out.println("INFO|" + String.format(msg, args));
    }

    public static void debug(String msg, Object... args) {
        System.out.println("DEBUG|" + String.format(msg, args));
    }

    public static void error(String msg, Object... args) {
        System.out.println("ERROR|" + String.format(msg, args));
    }

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
                this.debug = this.debug + 1 < 6 ? this.debug + 1 : 0;
            }
            default -> {
                // Nothing to do.
            }
        }
        sceneManager.getCurrent().onKeyReleased(e);
    }

    public Graphics2D getGraphics() {
        return buffer.createGraphics();
    }

    public static void main(String[] argc) {
        KarmaApp app = new KarmaApp();
        app.run(argc);
    }
}
