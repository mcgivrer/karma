package my.karma.app.behaviors;

import my.karma.app.KarmaPlatform;

import java.util.ArrayList;
import java.util.List;

public class PlaySoundOnCollisionBehavior implements KarmaPlatform.Behavior<KarmaPlatform.Entity> {


    private String soundName;
    private KarmaPlatform app;

    private List<KarmaPlatform.CollisionEvent> events = new ArrayList<>();

    public PlaySoundOnCollisionBehavior(KarmaPlatform app, String soundName) {
        this.app = app;
        this.soundName = soundName;
    }

    @Override
    public void onCollision(KarmaPlatform.CollisionEvent ce) {
        if (ce.getDst().name.startsWith("platform")) {
            if (!events.contains(ce)) {
                events.add(ce);
                KarmaPlatform.getSoundManager().playSound(soundName);
            }
        }
    }
}

