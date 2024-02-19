package my.karma.app.scenes;

import my.karma.app.ConfigService;
import my.karma.app.KConfigAttr;
import my.karma.app.KarmaPlatform;
import my.karma.app.behaviors.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static my.karma.app.KarmaPlatform.getSoundManager;

public class PlayScene extends KarmaPlatform.AbstractScene {

    private int lives = 5;
    private int score = 0;
    Dimension screenSize = ConfigService.get(KConfigAttr.RENDERING_BUFFER_SIZE);

    public PlayScene(KarmaPlatform app) {
        super(app);
    }

    @Override
    public String getTitle() {
        return "play";
    }

    @Override
    public void create(KarmaPlatform app) {

        KarmaPlatform.World w = getWorld();

        w.addDisturbance((KarmaPlatform.Disturbance)
            new KarmaPlatform.Disturbance("wind")
                .setPosition(0, 0)
                .setSize(w.getPlayArea().getWidth(), w.getPlayArea().getHeight() * 0.8)
                .addForce(new KarmaPlatform.Vector2D(0.0002, 0.0))
        );

        w.addDisturbance((KarmaPlatform.Disturbance)
            new KarmaPlatform.Disturbance("mag")
                .setPosition(0, 0)
                .setSize(w.getPlayArea().getWidth() * 0.15, w.getPlayArea().getHeight())
                .setForegroundColor(new Color(0.7f, 0.6f, 0.0f, 0.5f))
                .setBackgroundColor(new Color(0.7f, 0.6f, 0.0f, 0.5f))
                .addForce(new KarmaPlatform.Vector2D(-0.002, -0.012))
        );
        createPlatforms(app);


        // Add camera
        KarmaPlatform.Camera cam = new KarmaPlatform.Camera("camera_01")
            .setTweenFactor(0.2)
            .setViewport(new Rectangle2D.Double(0, 0, screenSize.width, screenSize.height));
        setCamera(cam);
        KarmaPlatform.Material playerMat = new KarmaPlatform.Material("PLAYER_MAT", 0.98, 1.0, 0.25);
        // Add a player.
        KarmaPlatform.Entity player = new KarmaPlatform.Entity("player")
            .setPosition(160, 30)
            .setPhysicType(KarmaPlatform.PhysicType.DYNAMIC)
            .setSize(16, 16)
            .setMaterial(playerMat)
            .setMass(20.0)
            .setForegroundColor(new Color(0.0f, 0.0f, 0.6f, 1.0f))
            .setBackgroundColor(Color.BLUE)
            .setPriority(1)
            .setAttribute("speedStep", 0.0045)
            .setAttribute("energy", 100.0)
            .setAttribute("mana", 100.0)
            .setAttribute("hit", 1.0)
            .addBehavior(new PlayerInputBehavior());
        addEntity(player);
        cam.setTarget(player);

        KarmaPlatform.Entity particleSystem = new KarmaPlatform.Entity("starfield")
            .setPhysicType(KarmaPlatform.PhysicType.NONE)
            .setPriority(-20)
            .setPosition(0, 0)
            .setStatic(true)
            .setForegroundColor(new Color(0.0f, 0.0f, 0.0f, 0.0f))
            .setBackgroundColor(new Color(0.0f, 0.0f, 0.0f, 0.0f))
            .setSize(screenSize.getWidth(), screenSize.getHeight())
            .addBehavior(new StarFieldParticleBehavior(player, 0.0005, 50, 30));
        addEntity(particleSystem);

        // Add some enemies.
        generateNRJBalls("ball", 20);

        // Create HUD
        createHUD(app);
    }

    private void createPlatforms(KarmaPlatform app) {
        KarmaPlatform.Material platformMat = new KarmaPlatform.Material("PLATFORM_MAT", 1.0, 1.0, 0.1);

        KarmaPlatform.Entity platform2 = new KarmaPlatform.Entity("platform_border_top")
            .setPosition(0, 0)
            .setSize((int) app.getWorld().getPlayArea().getWidth(), 16)
            .setPhysicType(KarmaPlatform.PhysicType.STATIC)
            .setType(KarmaPlatform.EntityType.RECTANGLE)
            .setMass(4.0)
            .setForegroundColor(Color.GRAY)
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
            .setForegroundColor(Color.GRAY)
            .setBackgroundColor(Color.DARK_GRAY)
            .setPriority(10)
            .setMaterial(platformMat);
        addEntity(platform3);

        KarmaPlatform.Entity platform4 = new KarmaPlatform.Entity("platform_border_left")
            .setPosition(0, 16)
            .setSize(16, (int) getWorld().getPlayArea().getHeight() - 16)
            .setPhysicType(KarmaPlatform.PhysicType.STATIC)
            .setType(KarmaPlatform.EntityType.RECTANGLE)
            .setMass(4.0)
            .setForegroundColor(Color.GRAY)
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
            .setForegroundColor(Color.GRAY)
            .setBackgroundColor(Color.DARK_GRAY)
            .setPriority(10)
            .setMaterial(platformMat);
        addEntity(platform5);

        List<KarmaPlatform.Entity> platforms = new ArrayList<>();

        for (int j = 0; j < 20; j++) {
            double maxX = getWorld().getPlayArea().getWidth() / 16.0;
            double maxY = getWorld().getPlayArea().getHeight() / 32.0;

            double pw = (2 + Math.random() * 4);
            double px = (2 + (Math.random() * (maxX - (4 + pw)))) * 16;
            double py = (1 + (Math.random() * (maxY - 2))) * 32;

            KarmaPlatform.Entity platform = new KarmaPlatform.Entity("platform_" + j)
                .setPosition(px, py)
                .setSize(pw * 16.0, 16.0)
                .setPhysicType(KarmaPlatform.PhysicType.STATIC)
                .setType(KarmaPlatform.EntityType.RECTANGLE)
                .setMass(4.0)
                .setForegroundColor(new Color(0.1f, 0.3f, 0.1f))
                .setBackgroundColor(new Color(0.2f, 0.6f, 0.2f))
                .setPriority(10)
                .setMaterial(platformMat);
            if (isNotIntersectingWith(platforms, platform)) {
                platforms.add(platform);
                addEntity(platform);
            }
        }
    }

    private boolean isNotIntersectingWith(List<KarmaPlatform.Entity> platforms, KarmaPlatform.Entity platform) {
        List<KarmaPlatform.Entity> collidingWith = platforms.stream()
            .filter(p -> p.box.intersects(platform.box))
            .toList();
        return collidingWith.isEmpty();
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
            .setPosition(screenSize.width - 20, 22)
            .setPhysicType(KarmaPlatform.PhysicType.NONE)
            .setPriority(101)
            .setStatic(true);
        addEntity(livesTxt);

        KarmaPlatform.TextObject heartTxt = (KarmaPlatform.TextObject) new KarmaPlatform.TextObject("heart")
            .setText("❤")
            .setFont(fsc)
            .setTextColor(Color.RED)
            .setPosition(screenSize.width - 30, 18)
            .setPhysicType(KarmaPlatform.PhysicType.NONE)
            .setPriority(100)
            .setStatic(true);
        addEntity(heartTxt);

        /*
        KarmaPlatform.GridObject go = (KarmaPlatform.GridObject) new KarmaPlatform.GridObject("grid")
                .setStrokeSize(0.5f)
                .setPriority(-10)
                .setForegroundColor(new Color(0.5f, 0.5f, 0.5f, 0.5f));
        addEntity(go);
         */
    }

    private void generateNRJBalls(String entityRootName, int nbBalls) {
        KarmaPlatform.Material ballMat = new KarmaPlatform.Material("BALL_MAT", 1.0, 1.0, 0.99);
        for (int i = 0; i < nbBalls; i++) {
            addEntity(
                new KarmaPlatform.Entity(entityRootName + "_" + i)
                    .setPosition(
                        32 + (Math.random() * (getWorld().getPlayArea().getWidth() - 64)),
                        32 + (Math.random() * (getWorld().getPlayArea().getHeight() - 64)))
                    .setSize(8, 8)
                    .setPhysicType(KarmaPlatform.PhysicType.DYNAMIC)
                    .setBackgroundColor(new Color(0.9f, 0.8f, 0.1f))
                    .setForegroundColor(new Color(0.7f, 0.6f, 0.2f))
                    .setType(KarmaPlatform.EntityType.ELLIPSE)
                    .setPriority(-i)
                    .setVelocity(
                        (0.5 - Math.random()) * 0.25,
                        (0.5 - Math.random()) * 0.25)
                    .setMaterial(ballMat)
                    .setMass(5.0)
                    .setAttribute("energy", 20.0)
                    .addBehavior(new BallOnCollisionBehavior(app))
                    .addBehavior(new PlaySoundOnCollisionBehavior(app, "toc", "platform"))
                    .addBehavior(new BallTrackingBehavior(app, 50.0)));
        }
    }

    @Override
    public void initialize(KarmaPlatform app) {
        score = 0;
        lives = 5;
        getSoundManager().loadSound("click", "/res/sounds/clic.wav");
        getSoundManager().loadSound("toc", "/res/sounds/toc.wav");
        getSoundManager().loadSound("tic", "/res/sounds/tic.wav");
    }

    @Override
    public void update(KarmaPlatform app, double d) {
        if (Optional.ofNullable(getEntity("lives")).isPresent()
            && Optional.ofNullable(getEntity("score")).isPresent()) {
            ((KarmaPlatform.TextObject) getEntity("lives")).setValue(lives);
            ((KarmaPlatform.TextObject) getEntity("score")).setValue(score);
        }
    }

    @Override
    public void draw(KarmaPlatform app, Graphics2D g) {
        //  retrieve some specific attributes from the player like energy and mana.
        Optional<KarmaPlatform.Entity> playerOption = Optional.ofNullable(getEntity("player"));
        if (playerOption.isPresent()) {
            KarmaPlatform.Entity player = playerOption.get();
            g.setStroke(new BasicStroke(1.0f));
            double energy = player.getAttribute("energy");
            drawGauge(g, Color.RED, app, screenSize.width - 80, 10, 40, 4, energy);

            double mana = player.getAttribute("mana");
            drawGauge(g, Color.BLUE, app, screenSize.width - 80, 16, 40, 4, mana);
        }

    }

    private static void drawGauge(Graphics2D g, Color red, KarmaPlatform app, int x, int y, int w, int h, double value) {
        g.setColor(red);
        g.fillRect(x, y, (int) ((value / 100.0) * w), h);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, (int) ((value / 100.0) * w), h);
    }

    @Override
    public void onKeyReleased(KeyEvent ke) {
        switch (ke.getKeyCode()) {
            case KeyEvent.VK_R -> {
                if (ke.isControlDown()) {
                    randomMove("ball_", 5.0);
                }
            }
            case KeyEvent.VK_PAGE_UP -> generateNRJBalls("ball_", 10);
            case KeyEvent.VK_PAGE_DOWN -> removeEnemies("ball_", 10);
            case KeyEvent.VK_DELETE -> removeEnemies("ball_", 0);
        }
    }

    private void randomMove(String entityNamefiltering, double randomFactor) {
        getEntities().stream()
            .filter(entity -> entity.isActive() && entity.name.startsWith(entityNamefiltering))
            .forEach(entity -> entity.setVelocity(
                new KarmaPlatform.Vector2D(
                    Math.random() * randomFactor,
                    Math.random() * randomFactor)));
    }

    @Override
    public String getName() {
        return "play";
    }

    private void removeEnemies(String entityNamefiltering, int nbEnemies) {
        List<KarmaPlatform.Entity> entitiesToDelete = getEntities().stream()
            .filter(e -> e.name.startsWith(entityNamefiltering))
            .toList();
        if (nbEnemies == 0) {
            entitiesToDelete.forEach(e -> getEntities().remove(e));
        } else {
            for (int i = 0; i < (Math.min(entitiesToDelete.size(), nbEnemies)); i++) {
                getEntities().remove(entitiesToDelete.get(i));
            }
        }
    }

}
