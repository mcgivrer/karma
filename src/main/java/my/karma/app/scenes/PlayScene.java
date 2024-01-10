package my.karma.app.scenes;

import my.karma.app.AbstractScene;
import my.karma.app.KarmaApp;

import java.awt.*;
import java.awt.event.KeyEvent;

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
        // Add a player.
        KarmaApp.Entity p = new KarmaApp.Entity("player")
                .setPosition(160, 30)
                .setPhysicType(KarmaApp.PhysicType.DYNAMIC)
                .setSize(16, 16)
                .setMaterial(new KarmaApp.Material(0.12, 1.0, 0.25))
                .setMass(20.0)
                .setBorderColor(new Color(0.0f, 0.0f, 0.6f, 1.0f))
                .setBackgroundColor(Color.BLUE)
                .setPriority(1)
                .addAttribute("speedStep", 0.15);
        addEntity(p);

        KarmaApp.Entity platform1 = new KarmaApp.Entity("platform_01")
                .setPosition(100, 100)
                .setSize(100, 32)
                .setPhysicType(KarmaApp.PhysicType.STATIC)
                .setType(KarmaApp.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(new KarmaApp.Material(1.0, 1.0, 0.1));
        addEntity(platform1);

        KarmaApp.Entity enemies = new KarmaApp.Entity("enemy_0")
                .setPosition(
                        (int) (Math.random() * getWorld().getPlayArea().getWidth()),
                        (int) (Math.random() * getWorld().getPlayArea().getHeight()))
                .setSize(8, 8)
                .setPhysicType(KarmaApp.PhysicType.DYNAMIC)
                .setBackgroundColor(Color.RED)
                .setType(KarmaApp.EntityType.ELLIPSE)
                .setBorderColor(new Color(0.8f, 0.0f, 0.0f, 1.0f))
                .setPriority(0)
                .setVelocity(
                        (0.5 - Math.random()) * 0.25,
                        (0.5 - Math.random()) * 0.25)
                .setMaterial(new KarmaApp.Material(1.0, 1.0, 0.25))
                .setMass(5.0);
        // Add some enemies.
        for (int i = 1; i < 20; i++) {
            enemies.add(
                    new KarmaApp.Entity("enemy_" + i)
                            .setPosition(
                                    (int) (Math.random() * getWorld().getPlayArea().getWidth()),
                                    (int) (Math.random() * getWorld().getPlayArea().getHeight()))
                            .setSize(8, 8)
                            .setPhysicType(KarmaApp.PhysicType.DYNAMIC)
                            .setBackgroundColor(Color.RED)
                            .setType(KarmaApp.EntityType.ELLIPSE)
                            .setBorderColor(new Color(0.8f, 0.0f, 0.0f, 1.0f))
                            .setPriority(-i)
                            .setVelocity(
                                    (0.5 - Math.random()) * 0.25,
                                    (0.5 - Math.random()) * 0.25)
                            .setMaterial(new KarmaApp.Material(1.0, 1.0, 0.99))
                            .setMass(5.0));
        }
        addEntity(enemies);

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
                .setPriority(100);

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
                .setPriority(101);
        addEntity(livesTxt);

        KarmaApp.TextObject heartTxt = (KarmaApp.TextObject) new KarmaApp.TextObject("heart")
                .setText("❤")
                .setFont(fsc)
                .setTextColor(Color.RED)
                .setPosition(app.getScreenSize().width - 30, 18)
                .setPhysicType(KarmaApp.PhysicType.NONE)
                .setPriority(100);
        addEntity(heartTxt);

        KarmaApp.GridObject go = (KarmaApp.GridObject) new KarmaApp.GridObject("grid")
                .setPriority(-10)
                .setBorderColor(Color.DARK_GRAY);
        addEntity(go);
    }

    @Override
    public void initialize(KarmaApp app) {
        score = 0;
        lives = 5;
    }

    @Override
    public void input(KarmaApp app) {
        KarmaApp.Entity p = getEntity("player");

        double speedStep = p.getAttribute("speedStep");

        if (app.isKeyPressed(KeyEvent.VK_UP)) {
            p.velocity.y = -speedStep;
        }
        if (app.isKeyPressed(KeyEvent.VK_DOWN)) {
            p.velocity.y = speedStep;

        }
        if (app.isKeyPressed(KeyEvent.VK_LEFT)) {
            p.velocity.x = -speedStep;
        }
        if (app.isKeyPressed(KeyEvent.VK_RIGHT)) {
            p.velocity.x = speedStep;
        }
        // process all input behaviors
        getEntities().stream().filter(e -> e.isActive()).forEach(e -> {
            if (!e.getBehaviors().isEmpty()) {
                e.getBehaviors().forEach(b -> {
                    b.onInput(app, e);
                });
            }
        });
    }

    @Override
    public void update(KarmaApp app, long d) {
        ((KarmaApp.TextObject) getEntity("lives")).setValue(lives);
        ((KarmaApp.TextObject) getEntity("score")).setValue(score);
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
        }
    }
}
