package my.karma.app.scenes;

import my.karma.app.AbstractScene;
import my.karma.app.KarmaPlatform;
import my.karma.app.behaviors.PlayerInputBehavior;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class PlayScene extends AbstractScene {

    private int lives = 5;
    private int score = 0;

    public PlayScene(KarmaPlatform app) {
        super(app);
    }

    @Override
    public String getTitle() {
        return "play";
    }

    @Override
    public void create(KarmaPlatform app) {

        createPlatforms(app);

        // Add camera
        KarmaPlatform.Camera cam = new KarmaPlatform.Camera("camera_01")
                .setTweenFactor(0.2)
                .setViewport(new Rectangle2D.Double(0, 0, app.getScreenSize().width, app.getScreenSize().height));
        setCamera(cam);
        KarmaPlatform.Material playerMat = new KarmaPlatform.Material("PLAYER_MAT", 0.98, 1.0, 0.25);
        // Add a player.
        KarmaPlatform.Entity p = new KarmaPlatform.Entity("player")
                .setPosition(160, 30)
                .setPhysicType(KarmaPlatform.PhysicType.DYNAMIC)
                .setSize(16, 16)
                .setMaterial(playerMat)
                .setMass(20.0)
                .setBorderColor(new Color(0.0f, 0.0f, 0.6f, 1.0f))
                .setBackgroundColor(Color.BLUE)
                .setPriority(1)
                .setAttribute("speedStep", 0.15)
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

    private void createPlatforms(KarmaPlatform app) {
        KarmaPlatform.Material platformMat = new KarmaPlatform.Material("PLATFORM_MAT", 1.0, 1.0, 0.1);
        KarmaPlatform.Entity platform1 = new KarmaPlatform.Entity("platform_01")
                .setPosition(100, 100)
                .setSize(100, 32)
                .setPhysicType(KarmaPlatform.PhysicType.STATIC)
                .setType(KarmaPlatform.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(platformMat);
        addEntity(platform1);

        KarmaPlatform.Entity platform2 = new KarmaPlatform.Entity("platform_border_top")
                .setPosition(0, 0)
                .setSize((int) app.getWorld().getPlayArea().getWidth(), 16)
                .setPhysicType(KarmaPlatform.PhysicType.STATIC)
                .setType(KarmaPlatform.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(platformMat);
        addEntity(platform2);

        KarmaPlatform.Entity platform3 = new KarmaPlatform.Entity("platform_border_bottom")
                .setPosition(0, getWorld().getPlayArea().getHeight() - 16)
                .setSize((int) getWorld().getPlayArea().getWidth(), 16)
                .setPhysicType(KarmaPlatform.PhysicType.STATIC)
                .setType(KarmaPlatform.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(platformMat);
        addEntity(platform3);

        KarmaPlatform.Entity platform4 = new KarmaPlatform.Entity("platform_border_left")
                .setPosition(0, 16)
                .setSize(16, (int) getWorld().getPlayArea().getHeight())
                .setPhysicType(KarmaPlatform.PhysicType.STATIC)
                .setType(KarmaPlatform.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(platformMat);
        addEntity(platform4);

        KarmaPlatform.Entity platform5 = new KarmaPlatform.Entity("platform_border_right")
                .setPosition((int) getWorld().getPlayArea().getWidth() - 16, 16)
                .setSize(16, (int) getWorld().getPlayArea().getHeight() - 16)
                .setPhysicType(KarmaPlatform.PhysicType.STATIC)
                .setType(KarmaPlatform.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(platformMat);
        addEntity(platform5);

    }

    private void createHUD(KarmaPlatform app) {
        // Add a HUD score display
        Font fsc = app.getGraphics().getFont().deriveFont(Font.BOLD, 18.0f);
        KarmaPlatform.TextObject score = (KarmaPlatform.TextObject) new KarmaPlatform.TextObject("score")
                .setText("")
                .setValue(0)
                .setFormat("%05d")
                .setFont(fsc)
                .setTextColor(Color.WHITE)
                .setPosition(10, 18)
                .setPhysicType(KarmaPlatform.PhysicType.NONE)
                .setPriority(100)
                .setStatic(true);

        addEntity(score);

        Font fl = app.getGraphics().getFont().deriveFont(Font.BOLD, 12.0f);
        KarmaPlatform.TextObject livesTxt = (KarmaPlatform.TextObject) new KarmaPlatform.TextObject("lives")
                .setText("")
                .setFormat("%d")
                .setValue(5)
                .setFont(fl)
                .setTextColor(Color.WHITE)
                .setPosition(app.getScreenSize().width - 20, 22)
                .setPhysicType(KarmaPlatform.PhysicType.NONE)
                .setPriority(101)
                .setStatic(true);
        addEntity(livesTxt);

        KarmaPlatform.TextObject heartTxt = (KarmaPlatform.TextObject) new KarmaPlatform.TextObject("heart")
                .setText("❤")
                .setFont(fsc)
                .setTextColor(Color.RED)
                .setPosition(app.getScreenSize().width - 30, 18)
                .setPhysicType(KarmaPlatform.PhysicType.NONE)
                .setPriority(100)
                .setStatic(true);
        addEntity(heartTxt);

        KarmaPlatform.GridObject go = (KarmaPlatform.GridObject) new KarmaPlatform.GridObject("grid")
                .setStrokeSize(0.5f)
                .setPriority(-10)
                .setBorderColor(new Color(0.5f, 0.5f, 0.5f, 0.5f));
        addEntity(go);
    }

    private void generateEnemies(int nbEnemies) {
        KarmaPlatform.Material enemyMat = new KarmaPlatform.Material("ENAMY_MAT", 1.0, 1.0, 0.99);
        for (int i = 0; i < nbEnemies; i++) {
            addEntity(
                    new KarmaPlatform.Entity("enemy_" + i)
                            .setPosition(
                                    16 + (Math.random() * (getWorld().getPlayArea().getWidth() - 32)),
                                    16 + (Math.random() * (getWorld().getPlayArea().getHeight()) - 32))
                            .setSize(8, 8)
                            .setPhysicType(KarmaPlatform.PhysicType.DYNAMIC)
                            .setBackgroundColor(Color.RED)
                            .setType(KarmaPlatform.EntityType.ELLIPSE)
                            .setBorderColor(new Color(0.8f, 0.0f, 0.0f, 1.0f))
                            .setPriority(-i)
                            .setVelocity(
                                    (0.5 - Math.random()) * 0.25,
                                    (0.5 - Math.random()) * 0.25)
                            .setMaterial(enemyMat)
                            .setMass(5.0)
                            .setAttribute("energy", 20.0)
                            .addBehavior(new KarmaPlatform.Behavior<>() {
                                @Override
                                public void onCollision(KarmaPlatform.CollisionEvent ce) {
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
                            }));
        }
    }

    @Override
    public void initialize(KarmaPlatform app) {
        score = 0;
        lives = 5;
    }

    @Override
    public void update(KarmaPlatform app, long d) {
        ((KarmaPlatform.TextObject) getEntity("lives")).setValue(lives);
        ((KarmaPlatform.TextObject) getEntity("score")).setValue(score);
    }

    @Override
    public void draw(KarmaPlatform app, Graphics2D g) {

        KarmaPlatform.Entity player = getEntity("player");
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
                            .forEach(entity -> entity.setVelocity(
                                    new KarmaPlatform.Vector2D(
                                            Math.random() * 5.0,
                                            Math.random() * 5.0)));
                }
            }
            case KeyEvent.VK_PAGE_UP -> generateEnemies(10);
            case KeyEvent.VK_PAGE_DOWN -> removeEnemies(10);
            case KeyEvent.VK_DELETE -> removeEnemies(0);
        }
    }

    private void removeEnemies(int nbEnemies) {
        List<KarmaPlatform.Entity> entitiesToDelete = getEntities().stream().filter(e -> e.name.startsWith("enemy_")).toList();
        if (nbEnemies == 0) {
            entitiesToDelete.forEach(e -> getEntities().remove(e));
        } else {
            for (int i = 0; i < (Math.min(entitiesToDelete.size(), nbEnemies)); i++) {
                getEntities().remove(entitiesToDelete.get(i));
            }
        }
    }

}
