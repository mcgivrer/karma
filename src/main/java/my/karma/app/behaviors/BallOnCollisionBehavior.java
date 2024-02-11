package my.karma.app.behaviors;

import my.karma.app.KarmaPlatform;

import java.awt.*;

public class BallOnCollisionBehavior implements KarmaPlatform.Behavior<KarmaPlatform.Entity> {
    private final KarmaPlatform app;

    public BallOnCollisionBehavior(KarmaPlatform app) {
        this.app = app;
    }

    @Override
    public void onCollision(KarmaPlatform.CollisionEvent ce) {
        KarmaPlatform.Entity src = ce.getSrc();
        KarmaPlatform.Entity dst = ce.getDst();
        // get src Entity energy
        if (src.getAttribute("energy") != null && dst.name.startsWith("player")) {

            int score = dst.getAttribute("score", 0);
            double energy = src.getAttribute("energy");
            // retrieve hit power from dst Entity if exists, else set 1
            double hit = dst.getAttribute("hit") != null ? dst.getAttribute("hit") : 0.1;
            // compute new energy for src Entity.
            energy -= hit;
            if (energy < 10) {
                src.setForegroundColor(Color.RED);
                src.setBackgroundColor(Color.ORANGE);
            }
            if (energy <= 0) {
                src.setActive(false);
                score += 10;
            }
            src.setAttribute("energy", energy);
            dst.setAttribute("score", score);
        }
    }
}
