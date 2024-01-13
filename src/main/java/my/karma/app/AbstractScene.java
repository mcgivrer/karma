package my.karma.app;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractScene implements KarmaApp.Scene {

    private final Map<String, KarmaApp.Entity> entities = new ConcurrentHashMap<>();
    private final KarmaApp.World world;
    private KarmaApp.Camera camera;

    public AbstractScene(KarmaApp app) {
        this.world = app.getWorld();
    }

    public void addEntity(KarmaApp.Entity e) {
        entities.put(e.name, e);
    }

    public KarmaApp.World getWorld() {
        return this.world;
    }

    public void clearEntities() {
        entities.clear();
    }

    public KarmaApp.Entity getEntity(String name) {
        return entities.get(name);
    }

    public Collection<KarmaApp.Entity> getEntities() {
        return entities.values();
    }

    public KarmaApp.Camera getCamera() {
        return this.camera;
    }

    public KarmaApp.Scene setCamera(KarmaApp.Camera c) {
        this.camera = c;
        return this;
    }
}
