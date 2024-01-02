package my.applicationname.app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * main class for project Karma
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class KarmaApp extends JPanel implements KeyListener {

    private ResourceBundle messages = ResourceBundle.getBundle("i18n.messages");
    private Properties config = new Properties();
    private boolean exit = false;
    private boolean keys[] = new boolean[1024];
    private JFrame frame;
    private BufferedImage buffer;
    private Dimension winSize;
    private Dimension resSize;
    private int strategyBufferNb;

    private String title = "WindApp";
    private Map<String, Entity> entities = new ConcurrentHashMap<>();
    private World world;

    public enum EntityType {
        RECTANGLE,
        ELLIPSE
    }

    public static class Entity {
        private static long index = 0;
        private boolean active = true;
        long id = index++;
        String name;
        double x, y;
        int w, h;
        double dx = 0, dy = 0;
        private double elasticity = 1.0;
        private double friction = 1.0;
        Color fc = Color.WHITE, bg = Color.BLUE;
        Rectangle2D box = new Rectangle2D.Double();
        private int priority = 1;
        private Map<String, Object> attributes = new ConcurrentHashMap<>();
        private EntityType type = EntityType.RECTANGLE;

        public Entity(String name) {
            this.name = name;
        }

        public Entity(String name, int w, int h) {
            this(name);
            setSize(w, h);
        }

        public Entity(String name, int x, int y, int w, int h) {
            this(name);
            setPosition(x, y);
            setSize(w, h);
        }

        public Entity setPosition(int x, int y) {
            this.x = x;
            this.y = y;
            updateBox();
            return this;
        }

        public Entity setSize(int w, int h) {
            this.w = w;
            this.h = h;
            updateBox();
            return this;
        }

        public void updateBox() {
            box.setFrame(x, y, w, h);
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

        public Entity setFriction(double f) {
            this.friction = f;
            return this;
        }

        public double getFriction() {
            return friction;
        }

        public Entity setElasticity(double e) {
            this.elasticity = e;
            return this;
        }

        public double getElasticity() {
            return elasticity;
        }

        public <T> Entity addAttribute(String attrName, T attrValue) {
            this.attributes.put(attrName, attrValue);
            return this;
        }

        public <T> T getAttribute(String attrName) {
            return (T) attributes.get(attrName);
        }

        public Entity setVelocity(double dx, double dy) {
            this.dx = dx;
            this.dy = dy;
            return this;
        }

        public Entity setType(EntityType entityType) {
            this.type = entityType;
            return this;
        }

        public EntityType getType() {
            return type;
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

    public KarmaApp() {
        System.out.printf("Initialization application %s (%s)%n",
                messages.getString("app.name"),
                messages.getString("app.version"));
    }

    public void run(String[] args) {
        init(args);
        loop();
        dispose();
    }

    private void init(String[] args) {
        // get configuration values.
        loadConfiguration(args);
        // Create window
        frame = new JFrame(title);
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
    }

    private void loadConfiguration(String[] args) {
        List<String> lArgs = Arrays.asList(args);
        try {
            config.load(this.getClass().getResourceAsStream("/config.properties"));

            List<String> propertyList =
                    config.entrySet().stream()
                            .map(e -> String.valueOf(e.getKey()) + "=" + String.valueOf(e.getValue()))
                            .collect(Collectors.toList());
            parseConfig(propertyList);

        } catch (IOException e) {
            error("unable to read configuration file: %s", e.getMessage());
        }
        if (!lArgs.isEmpty()) {
            parseConfig(lArgs);
            lArgs.forEach(s -> {
                info("- arg: %s", s);
            });

        }
    }

    private void parseConfig(List<String> lArgs) {
        lArgs.forEach(s -> {
            String[] arg = s.split("=");
            switch (arg[0]) {
                case "app.exit" -> exit = Boolean.parseBoolean(arg[1]);
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

                default -> error("Unknown %s attribute ", s);
            }
        });
    }

    private void loop() {
        createScene();
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

    private void createScene() {
        Entity p = new Entity("player", 160, 100, 16, 16)
                .setFriction(0.995)
                .setElasticity(0.45)
                .setBorderColor(new Color(0.0f, 0.0f, 0.6f, 1.0f))
                .setBackgroundColor(Color.BLUE)
                .setPriority(1)
                .addAttribute("speedStep", 0.15);
        addEntity(p);

        for (int i = 0; i < 20; i++) {
            addEntity(
                    new Entity("enemy_" + i,
                            (int) (Math.random() * world.getPlayArea().getWidth()),
                            (int) (Math.random() * world.getPlayArea().getHeight()),
                            8, 8)
                            .setBackgroundColor(Color.RED)
                            .setType(EntityType.ELLIPSE)
                            .setBorderColor(new Color(0.8f, 0.0f, 0.0f, 1.0f))
                            .setPriority(-i)
                            .setVelocity(
                                    (0.5 - Math.random()) * 0.25,
                                    (0.5 - Math.random()) * 0.25)
                            .setElasticity(1.0));
        }
    }

    private void addEntity(Entity e) {
        entities.put(e.name, e);
    }

    private void draw() {
        Graphics2D g = buffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // Clear display area
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        // draw temporary background
        g.setColor(Color.DARK_GRAY);
        for (double dx = 0; dx < world.getPlayArea().getWidth(); dx += 16.0) {
            g.drawRect((int) dx, 0, 16, (int) world.getPlayArea().getHeight());
        }
        for (double dy = 0; dy < world.getPlayArea().getHeight(); dy += 16) {
            g.drawRect(0, (int) dy, (int) world.getPlayArea().getWidth(), 16);
        }
        // Draw things
        entities.values().stream()
                .filter(Entity::isActive)
                .sorted(Comparator.comparingInt(Entity::getPriority))
                .forEach(e -> {
                    switch (e.type) {
                        case RECTANGLE -> {
                            g.setColor(e.bg);
                            g.fillRect((int) e.x, (int) e.y, e.w, e.h);
                            g.setColor(e.fc);
                            g.drawRect((int) e.x, (int) e.y, e.w, e.h);

                        }
                        case ELLIPSE -> {
                            g.setColor(e.bg);
                            g.fillOval((int) e.x, (int) e.y, e.w, e.h);
                            g.setColor(e.fc);
                            g.drawOval((int) e.x, (int) e.y, e.w, e.h);
                        }
                    }
                });
        g.dispose();
        // Copy buffer to window.
        BufferStrategy bs = frame.getBufferStrategy();
        Graphics2D gs = (Graphics2D) bs.getDrawGraphics();
        gs.drawImage(buffer,
                0, 32, winSize.width + 16, winSize.height + 32,
                0, 0, buffer.getWidth(), buffer.getHeight(),
                null);
        bs.show();
        gs.dispose();
    }

    private void update(long d) {
        entities.values().stream()
                .sorted(Comparator.comparingInt(Entity::getPriority))
                .forEach(e -> {
                            applyPhysics(e, world, d);
                        }
                );
    }

    private void applyPhysics(Entity e, World w, long d) {
        // apply velocity computation
        e.x += e.dx * d;
        e.y += e.dy * d;
        e.updateBox();

        // apply friction
        e.dx = e.dx * e.getFriction();
        e.dy = e.dy * e.getFriction();

        // keep entity in the game area
        keepInPlayArea(w, e);
        detectCollision(w, e);
    }

    private void detectCollision(World w, Entity e) {
        entities.values().stream().filter(o -> e.isActive() && o.isActive() && !o.equals(e)).forEach(o -> {
            if (e.box.intersects(o.box) || e.box.contains(o.box)) {
                e.dx = Math.max(o.dx, e.dx) * -Math.max(e.getElasticity(), o.getElasticity());
                e.dy = Math.max(o.dy, e.dy) * -Math.max(e.getElasticity(), o.getElasticity());

                double ex=e.x-o.x;
                double ey = e.y-o.y;
                if(ex<0){
                    
                }
            }
        });
    }

    private void keepInPlayArea(World w, Entity e) {
        Rectangle2D playArea = w.getPlayArea();
        if (!playArea.contains(e.box)) {
            if (e.x < playArea.getX()) {
                e.dx = e.dx * -e.getElasticity();
                e.x = playArea.getX();
            }
            if (e.y < playArea.getY()) {
                e.dy = e.dy * -e.getElasticity();
                e.y = playArea.getY();
            }
            if (e.x + e.w > playArea.getX() + playArea.getWidth()) {
                e.dx = e.dx * -e.getElasticity();
                e.x = playArea.getX() + playArea.getWidth() - e.w;
            }
            if (e.y + e.h > playArea.getY() + playArea.getHeight()) {
                e.dy = e.dy * -e.getElasticity();
                e.y = playArea.getY() + playArea.getHeight() - e.h;
            }
            e.updateBox();
        }
    }

    private void input() {
        Entity p = entities.get("player");

        double speedStep = p.getAttribute("speedStep");

        if (keys[KeyEvent.VK_UP]) {
            p.dy = -speedStep;
        }
        if (keys[KeyEvent.VK_DOWN]) {
            p.dy = speedStep;

        }
        if (keys[KeyEvent.VK_LEFT]) {
            p.dx = -speedStep;
        }
        if (keys[KeyEvent.VK_RIGHT]) {
            p.dx = speedStep;
        }
    }

    private void dispose() {
        info("End of application Wind");
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
            case KeyEvent.VK_ESCAPE -> {
                this.exit = true;
            }
            case KeyEvent.VK_Z -> {
                this.entities.clear();
                createScene();
            }
            default -> {
                // Nothing to do.
            }
        }
    }

    public static void main(String[] argc) {
        KarmaApp app = new KarmaApp();
        app.run(argc);
    }
}
