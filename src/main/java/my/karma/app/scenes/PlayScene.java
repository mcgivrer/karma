package my.karma.app.scenes;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import my.karma.app.AbstractScene;
import my.karma.app.KarmaApp;
import my.karma.app.behaviors.PlayerInputBehavior;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.Collectors;

public class PlayScene extends AbstractScene {

    private int lives = 5;
    private int score = 0;

    public PlayScene(KarmaApp app) {
        super(app);
    }

    @Override
    public String getTitle() {
        return "play";
    }

    @Override
    public void create(KarmaApp app) {
        getWorld().setGravity(0.981);

        createPlatforms(app);

        // Add camera
        KarmaApp.Camera cam = new KarmaApp.Camera("camera_01")
                .setTweenFactor(0.2)
                .setViewport(new Rectangle2D.Double(0, 0, app.getScreenSize().width, app.getScreenSize().height));
        setCamera(cam);

        // Add a player.
        KarmaApp.Entity p = new KarmaApp.Entity("player")
                .setPosition(160, 30)
                .setPhysicType(KarmaApp.PhysicType.DYNAMIC)
                .setSize(16, 16)
                .setMaterial(new KarmaApp.Material(0.9998, 1.0, 0.85))
                .setMass(20.0)
                .setBorderColor(new Color(0.0f, 0.0f, 0.6f, 1.0f))
                .setBackgroundColor(Color.BLUE)
                .setPriority(1)
                .setAttribute("speedStep", 0.0045)
                .setAttribute("energy", 100.0)
                .setAttribute("mana", 100.0)
                .setAttribute("hit", 1.0)
                .addBehavior(new PlayerInputBehavior());
        addEntity(p);
        cam.setTarget(p);

        // Add some enemies.
        generateEnemies(20);

        // Create HUD
        createHUD(app);
    }

    private void createPlatforms(KarmaApp app) {
        KarmaApp.Material platformMat = new KarmaApp.Material(1.0, 1.0, 0.89);

        KarmaApp.Entity platform2 = new KarmaApp.Entity("platform_border_top")
                .setPosition(0, 0)
                .setSize((int) app.getWorld().getPlayArea().getWidth(), 16)
                .setPhysicType(KarmaApp.PhysicType.STATIC)
                .setType(KarmaApp.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(platformMat);
        addEntity(platform2);

        KarmaApp.Entity platform3 = new KarmaApp.Entity("platform_border_bottom")
                .setPosition(0, getWorld().getPlayArea().getHeight() - 16)
                .setSize((int) getWorld().getPlayArea().getWidth(), 16)
                .setPhysicType(KarmaApp.PhysicType.STATIC)
                .setType(KarmaApp.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(platformMat);
        addEntity(platform3);

        KarmaApp.Entity platform4 = new KarmaApp.Entity("platform_border_left")
                .setPosition(0, 16)
                .setSize(16, (int) getWorld().getPlayArea().getHeight() - 16)
                .setPhysicType(KarmaApp.PhysicType.STATIC)
                .setType(KarmaApp.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(platformMat);
        addEntity(platform4);

        KarmaApp.Entity platform5 = new KarmaApp.Entity("platform_border_right")
                .setPosition((int) getWorld().getPlayArea().getWidth() - 16, 16)
                .setSize(16, (int) getWorld().getPlayArea().getHeight() - 16)
                .setPhysicType(KarmaApp.PhysicType.STATIC)
                .setType(KarmaApp.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(platformMat);
        addEntity(platform5);
        for (int j = 0; j < 10; j++) {
            KarmaApp.Entity platform1 = new KarmaApp.Entity("platform_" + j)
                    .setPosition(
                            (Math.random() * (getWorld().getPlayArea().getWidth() / 16.0) * 16.0),
                            (Math.random() * (getWorld().getPlayArea().getHeight() / 16.0) * 16.0))
                    .setSize(
                            (int) (Math.random() * 5 * 16.0), 16)
                    .setPhysicType(KarmaApp.PhysicType.STATIC)
                    .setType(KarmaApp.EntityType.RECTANGLE)
                    .setMass(4.0)
                    .setBorderColor(Color.GRAY)
                    .setBackgroundColor(Color.DARK_GRAY)
                    .setPriority(10)
                    .setMaterial(platformMat);
            addEntity(platform1);
        }
    }

    private void createHUD(KarmaApp app) {
        // Add a HUD score display
        Font fsc = app.getGraphics().getFont().deriveFont(Font.BOLD, 18.0f);
        KarmaApp.TextObject score = (KarmaApp.TextObject) new KarmaApp.TextObject("score")
                .setText("")
                .setValue(0)
                .setFormat("%05d")
                .setFont(fsc)
                .setTextColor(Color.WHITE)
                .setPosition(10, 18)
                .setPhysicType(KarmaApp.PhysicType.NONE)
                .setPriority(100)
                .setStatic(true);

        addEntity(score);

        Font fl = app.getGraphics().getFont().deriveFont(Font.BOLD, 12.0f);
        KarmaApp.TextObject livesTxt = (KarmaApp.TextObject) new KarmaApp.TextObject("lives")
                .setText("")
                .setFormat("%d")
                .setValue(5)
                .setFont(fl)
                .setTextColor(Color.WHITE)
                .setPosition(app.getScreenSize().width - 20, 22)
                .setPhysicType(KarmaApp.PhysicType.NONE)
                .setPriority(101)
                .setStatic(true);
        addEntity(livesTxt);

        KarmaApp.TextObject heartTxt = (KarmaApp.TextObject) new KarmaApp.TextObject("heart")
                .setText("❤")
                .setFont(fsc)
                .setTextColor(Color.RED)
                .setPosition(app.getScreenSize().width - 30, 18)
                .setPhysicType(KarmaApp.PhysicType.NONE)
                .setPriority(100)
                .setStatic(true);
        addEntity(heartTxt);

        KarmaApp.GridObject go = (KarmaApp.GridObject) new KarmaApp.GridObject("grid")
                .setStrokeSize(0.5f)
                .setPriority(-10)
                .setBorderColor(new Color(0.5f, 0.5f, 0.5f, 0.5f));
        addEntity(go);
    }

    private void generateEnemies(int nbEnemies) {
        for (int i = 0; i < nbEnemies; i++) {
            addEntity(
                    new KarmaApp.Entity("enemy_" + i)
                            .setPosition(
                                    (int) 16 + (Math.random() * (getWorld().getPlayArea().getWidth() - 32)),
                                    (int) 16 + (Math.random() * (getWorld().getPlayArea().getHeight()) - 32))
                            .setSize(8, 8)
                            .setPhysicType(KarmaApp.PhysicType.DYNAMIC)
                            .setBackgroundColor(Color.RED)
                            .setType(KarmaApp.EntityType.ELLIPSE)
                            .setBorderColor(new Color(0.8f, 0.0f, 0.0f, 1.0f))
                            .setPriority(-i)
                            .setVelocity(
                                    (0.5 - Math.random()) * 0.25,
                                    (0.5 - Math.random()) * 0.25)
                            .setMaterial(new KarmaApp.Material(0.98, 1.0, 0.99))
                            .setMass(5.0)
                            .setAttribute("energy", 20.0)
                            .addBehavior(new KarmaApp.Behavior<KarmaApp.Entity>() {
                                @Override
                                public void onCollision(KarmaApp.CollisionEvent ce) {
                                    // get src Entity energy
                                    if (ce.getSrc().getAttribute("energy") != null && ce.getDst().name.startsWith("player")) {
                                        double energy = ce.getSrc().getAttribute("energy");
                                        // retrieve hit power from dst Entity if exists, else set 1
                                        double hit = ce.getDst().getAttribute("hit") != null ? ce.getDst().getAttribute("hit") : 0.1;
                                        // compute new energy for src Entity.
                                        energy -= hit;
                                        if (energy < 10) {
                                            ce.getSrc().setBorderColor(Color.RED);
                                            ce.getSrc().setBackgroundColor(Color.ORANGE);
                                        }
                                        if (energy <= 0) {
                                            ce.getSrc().setActive(false);
                                            score += 10;
                                        }
                                        ce.getSrc().setAttribute("energy", energy);
                                    }
                                }
                            }).addBehavior(new KarmaApp.Behavior<KarmaApp.Entity>() {
                                @Override
                                public void onUpdate(KarmaApp a, KarmaApp.Entity e, double d) {
                                    KarmaApp.Entity player = getEntity("player");
                                    if (player.getCenter().getDistance(e.getCenter()) < 50.0) {
                                        KarmaApp.Vector2D v = player.getVelocity().subtract(e.getPosition());
                                        e.setVelocity(v.multiply(-0.0001));
                                    }
                                }

                                @Override
                                public void onDraw(KarmaApp a, Graphics2D g, KarmaApp.Entity e) {
                                    if (KarmaApp.isDebugGreaterThan(3)) {
                                        g.setColor(Color.YELLOW);
                                        g.setStroke(new BasicStroke(0.05f));
                                        g.draw(
                                                new Ellipse2D.Double(
                                                        (int) e.getCenter().x - e.w, (int) e.getCenter().y - e.h,
                                                        (int) 50.0, (int) 50.0));
                                    }

                                }
                            }));
        }
    }

    @Override
    public void initialize(KarmaApp app) {
        score = 0;
        lives = 5;
    }

    @Override
    public void update(KarmaApp app, double d) {
        ((KarmaApp.TextObject) getEntity("lives")).setValue(lives);
        ((KarmaApp.TextObject) getEntity("score")).setValue(score);
    }

    @Override
    public void draw(KarmaApp app, Graphics2D g) {

        KarmaApp.Entity player = getEntity("player");
        g.setStroke(new BasicStroke(1.0f));
        double energy = player.getAttribute("energy");
        g.setColor(Color.RED);
        g.fillRect(app.getScreenSize().width - 80, 10, (int) ((energy / 100.0) * 40.0), 4);
        g.setColor(Color.BLACK);
        g.drawRect(app.getScreenSize().width - 80, 10, (int) ((energy / 100.0) * 40.0), 4);

        double mana = player.getAttribute("mana");
        g.setColor(Color.BLUE);
        g.fillRect(app.getScreenSize().width - 80, 16, (int) ((mana / 100.0) * 40.0), 4);
        g.setColor(Color.BLACK);
        g.drawRect(app.getScreenSize().width - 80, 16, (int) ((mana / 100.0) * 40.0), 4);


    }

    @Override
    public void onKeyReleased(KeyEvent ke) {
        switch (ke.getKeyCode()) {
            case KeyEvent.VK_R -> {
                if (ke.isControlDown()) {
                    getEntities().stream()
                            .filter(entity -> entity.isActive() && entity.name.startsWith("enemy_"))
                            .forEach(entity -> {
                                entity.setVelocity(
                                        new KarmaApp.Vector2D(
                                                Math.random() * 5.0,
                                                Math.random() * 5.0));
                            });
                }
            }
            case KeyEvent.VK_PAGE_UP -> {
                generateEnemies(10);
            }
            case KeyEvent.VK_PAGE_DOWN -> {
                removeEnemies(10);
            }
            case KeyEvent.VK_DELETE -> {
                removeEnemies(0);
            }

        }
    }

    private void removeEnemies(int nbEnemies) {
        List<KarmaApp.Entity> entitiesToDelete = getEntities().stream().filter(e -> e.name.startsWith("enemy_")).toList();
        if (nbEnemies == 0) {
            entitiesToDelete.forEach(e -> getEntities().remove(e));
        } else {
            for (int i = 0; i < (Math.min(entitiesToDelete.size(), nbEnemies)); i++) {
                getEntities().remove(entitiesToDelete.get(i));
            }
        }
    }

}
