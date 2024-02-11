package my.karma.app.behaviors;

import my.karma.app.KarmaPlatform;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Optional;

public class BallTrackingBehavior implements KarmaPlatform.Behavior<KarmaPlatform.Entity> {

    private final KarmaPlatform app;
    private final double detectionThreshold;

    public BallTrackingBehavior(KarmaPlatform app, double detectionThreshold) {
        this.app = app;
        this.detectionThreshold = detectionThreshold;
    }

    @Override
    public void onUpdate(KarmaPlatform a, KarmaPlatform.Entity e, double d) {
        KarmaPlatform.Scene scene = app.getSceneManager().getCurrent();
        Optional<KarmaPlatform.Entity> player = Optional.ofNullable(scene.getEntity("player"));
        if (player.isPresent()
            && Math.abs(player.get().getCenter().getDistance(e.getCenter())) < detectionThreshold) {
            KarmaPlatform.Vector2D v = player.get().getVelocity().subtract(e.getPosition());
            e.setVelocity(v.multiply(-0.0001));
        }
    }

    @Override
    public void onDraw(KarmaPlatform a, Graphics2D g, KarmaPlatform.Entity e) {
        if (KarmaPlatform.isDebugGreaterThan(3)) {
            g.setColor(Color.YELLOW);
            g.setStroke(new BasicStroke(0.05f));
            g.draw(
                new Ellipse2D.Double(
                    (int) e.getCenter().x - e.w, (int) e.getCenter().y - e.h,
                    (int) 50.0, (int) 50.0));
        }

    }
}
