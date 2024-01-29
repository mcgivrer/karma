package my.karma.app.behaviors;

import my.karma.app.KarmaPlatform;

import java.awt.event.KeyEvent;

public class PlayerInputBehavior implements KarmaPlatform.Behavior<KarmaPlatform.Entity> {
    @Override
    public void onInput(KarmaPlatform app, KarmaPlatform.Entity p) {

        double speedStep = p.getAttribute("speedStep");

        if (app.isKeyPressed(KeyEvent.VK_UP)) {
            p.addForce(new KarmaPlatform.Vector2D(0, -speedStep*3.0));
        }
        if (app.isKeyPressed(KeyEvent.VK_DOWN)) {
            p.addForce(new KarmaPlatform.Vector2D(0, speedStep));

        }
        if (app.isKeyPressed(KeyEvent.VK_LEFT)) {
            p.addForce(new KarmaPlatform.Vector2D(-speedStep, 0));

        }
        if (app.isKeyPressed(KeyEvent.VK_RIGHT)) {
            p.addForce(new KarmaPlatform.Vector2D(speedStep, 0));
        }
    }
}
