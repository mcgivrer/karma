package my.karma.app.behaviors;

import my.karma.app.KarmaApp;

import java.awt.event.KeyEvent;

public class PlayerInputBehavior implements KarmaApp.Behavior<KarmaApp.Entity> {
    @Override
    public void onInput(KarmaApp app, KarmaApp.Entity p) {

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
