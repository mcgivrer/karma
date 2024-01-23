package my.karma.app;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractScene implements KarmaPlatform.Scene {

    private final Map<String, KarmaPlatform.Entity> entities = new ConcurrentHashMap<>();
    private final KarmaPlatform.World world;
    private KarmaPlatform.Camera camera;

    public AbstractScene(KarmaPlatform app) {
        this.world = app.getWorld();
    }

    public void addEntity(KarmaPlatform.Entity e) {
        entities.put(e.name, e);
    }

    public KarmaPlatform.World getWorld() {
        return this.world;
    }

    public void clearEntities() {
        entities.clear();
    }

    public KarmaPlatform.Entity getEntity(String name) {
        return entities.get(name);
    }

    public Collection<KarmaPlatform.Entity> getEntities() {
        return entities.values();
    }

    public KarmaPlatform.Camera getCamera() {
        return this.camera;
    }

    public KarmaPlatform.Scene setCamera(KarmaPlatform.Camera c) {
        this.camera = c;
        return this;
    }
}
