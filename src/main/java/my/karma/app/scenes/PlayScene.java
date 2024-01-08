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
                .setPosition(160, 100)
                .setSize(16, 16)
                .setFriction(0.995)
                .setElasticity(0.45)
                .setBorderColor(new Color(0.0f, 0.0f, 0.6f, 1.0f))
                .setBackgroundColor(Color.BLUE)
                .setPriority(1)
                .addAttribute("speedStep", 0.15);
        addEntity(p);

        // Add some enemies.
        for (int i = 0; i < 20; i++) {
            addEntity(
                    new KarmaApp.Entity("enemy_" + i)
                            .setPosition(
                                    (int) (Math.random() * getWorld().getPlayArea().getWidth()),
                                    (int) (Math.random() * getWorld().getPlayArea().getHeight()))
                            .setSize(8, 8)
                            .setBackgroundColor(Color.RED)
                            .setType(KarmaApp.EntityType.ELLIPSE)
                            .setBorderColor(new Color(0.8f, 0.0f, 0.0f, 1.0f))
                            .setPriority(-i)
                            .setVelocity(
                                    (0.5 - Math.random()) * 0.25,
                                    (0.5 - Math.random()) * 0.25)
                            .setElasticity(1.0));
        }

        // Add a HUD score display
        Font fsc = app.getGraphics().getFont().deriveFont(Font.BOLD, 18.0f);
        KarmaApp.TextObject score = (KarmaApp.TextObject) new KarmaApp.TextObject("score")
                .setText("")
                .setValue(0)
                .setFormat("%05d")
                .setFont(fsc)
                .setTextColor(Color.WHITE)
                .setPosition(10, 18)
                .setPhysicType(KarmaApp.PhysicType.NONE);
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
                .setPriority(2);
        addEntity(livesTxt);

        KarmaApp.TextObject heartTxt = (KarmaApp.TextObject) new KarmaApp.TextObject("heart")
                .setText("❤")
                .setFont(fsc)
                .setTextColor(Color.RED)
                .setPosition(app.getScreenSize().width - 30, 18)
                .setPhysicType(KarmaApp.PhysicType.NONE)
                .setPriority(1);
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
            p.dy = -speedStep;
        }
        if (app.isKeyPressed(KeyEvent.VK_DOWN)) {
            p.dy = speedStep;

        }
        if (app.isKeyPressed(KeyEvent.VK_LEFT)) {
            p.dx = -speedStep;
        }
        if (app.isKeyPressed(KeyEvent.VK_RIGHT)) {
            p.dx = speedStep;
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
}
