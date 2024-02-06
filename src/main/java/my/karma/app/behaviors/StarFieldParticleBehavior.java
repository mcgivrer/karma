package my.karma.app.behaviors;

import my.karma.app.KarmaPlatform;

import java.awt.*;
import java.util.Optional;

public class StarFieldParticleBehavior implements KarmaPlatform.ParticleBehavior<KarmaPlatform.Entity> {


    private int nbStars;
    private KarmaPlatform.Entity focusEntity;
    private double speedRatio;
    private double starSpread;

    public StarFieldParticleBehavior(KarmaPlatform.Entity focusEntity, double speedRatio, int nbStars, double starSpread) {
        this.speedRatio = speedRatio;
        this.focusEntity = focusEntity;
        this.nbStars = nbStars;
        this.starSpread = starSpread;
    }

    public StarFieldParticleBehavior() {
        speedRatio = 0.001;
        starSpread = 30;
        nbStars = 20;
    }

    @Override
    public void onUpdate(KarmaPlatform a, KarmaPlatform.Entity e, double d) {
        if (Optional.ofNullable(focusEntity).isEmpty()) {
            focusEntity = a.getSceneManager().getCurrent().getEntity("player");
        }

        if (e.getChild().size() < 200) {
            this.onCreate(a, e);
        }
        for (KarmaPlatform.Entity c : e.getChild()) {
            double dx = -focusEntity.getVelocity().x;
            double dy = -focusEntity.getVelocity().y;
            c.setPosition(
                c.getPosition().x + (speedRatio * dx * c.getPriority()),
                c.getPosition().y + (speedRatio * dy * c.getPriority())
            );
            if (!a.getWorld().getPlayArea().contains(c.box)) {
                if (c.getPosition().x > a.getWorld().getPlayArea().getWidth()) {
                    c.getPosition().x = 0;
                }
                if (c.getPosition().x < 0) {
                    c.getPosition().x = a.getWorld().getPlayArea().getWidth();
                }
                if (c.getPosition().y > a.getWorld().getPlayArea().getHeight()) {
                    c.getPosition().y = 0;
                }
                if (c.getPosition().y < 0) {
                    c.getPosition().y = a.getWorld().getPlayArea().getHeight();
                }
            }
        }
    }

    @Override
    public void onDraw(KarmaPlatform a, Graphics2D g, KarmaPlatform.Entity e) {
        e.getChild().forEach(c -> {
            g.setColor(c.getBackgroundColor());
            g.drawRect((int) c.box.getWidth(), (int) c.box.getHeight(),
                (int) c.size.getX(), (int) c.size.getY());
        });
    }

    @Override
    public void onCreate(KarmaPlatform a, KarmaPlatform.Entity e) {
        for (int i = 0; i < nbStars; i++) {
            KarmaPlatform.Entity particle = new KarmaPlatform.Entity(e.name + "_P" + e.id)
                .setPosition(
                    a.getWorld().getPlayArea().getWidth() * Math.random(),
                    a.getWorld().getPlayArea().getHeight() * Math.random())
                .setSize(0.5, 0.5)
                .setPhysicType(KarmaPlatform.PhysicType.NONE)
                .setStatic(true)
                .setMass(1.0)
                .setForegroundColor(new Color(1.0f, 1.0f, 1.0f, 0.5f))
                .setBackgroundColor(new Color(1.0f, 1.0f, 1.0f, 0.5f))
                .setPriority(e.getPriority() + (int) (Math.random() * starSpread) + 1);

            e.getChild().add(particle);
            if (!e.box.contains(particle.box)) {
                e.box.add(particle.box);
            }
            e.setSize(e.box.getWidth(), e.box.getHeight());
        }

    }
}
