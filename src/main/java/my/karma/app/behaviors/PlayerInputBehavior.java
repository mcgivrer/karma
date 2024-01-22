package my.karma.app.behaviors;

import my.karma.app.KarmaPlatform;

import java.awt.event.KeyEvent;

public class PlayerInputBehavior implements KarmaPlatform.Behavior<KarmaPlatform.Entity> {
    @Override
    public void onInput(KarmaPlatform app, KarmaPlatform.Entity p) {

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
    }
}
