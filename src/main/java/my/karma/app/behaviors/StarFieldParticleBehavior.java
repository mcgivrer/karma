package my.karma.app.behaviors;

import my.karma.app.KarmaPlatform;

import java.awt.*;

public class StarFieldParticleBehavior implements KarmaPlatform.ParticleBehavior<KarmaPlatform.Entity> {
    @Override
    public void onUpdate(KarmaPlatform a, KarmaPlatform.Entity e, double d) {

        if (e.getChild().size() < 200) {
            this.onCreate(a, e);
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
        for (int i = 0; i < 20; i++) {
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
                .setPriority(e.getPriority() + 1);

            e.getChild().add(particle);
            if (!e.box.contains(particle.box)) {
                e.box.add(particle.box);
            }
            e.setSize(e.box.getWidth(), e.box.getHeight());
        }

    }
}
