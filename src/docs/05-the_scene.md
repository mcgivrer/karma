# The Scene

When it comes to building a game, there are multiple steps im its scenery.
You start the game, and it appears on the Title screen, then you navigate to some menu to start
the adventure, the game screen opens, and you play sometime, until you lost or were killed inm the game.

Each of those states contains different play type. If you want to implement all of these, the complexity may
raise too much.

The best way to solve this complexity is to split each level into subprograms.

This is where, like in a movie, we will use some scenes!

## Scene interface

Now we have a way to go,let's standardize the way we will implement a state: a `Scene`.

```java
public interface Scene {
    // <1>
    String getTitle();

    // <2>
    void create(KarmaApp app);

    // <3>
    void initialize(KarmaApp app);

    // <4>
    void input();

    // <5>
    void update(KarmaApp app, long d);

    // <6>
    void draw(KarmaApp app, Graphics2D g);

    // <7>
    void dispose(KarmaApp app);

    // <8>
    Collection<Entity> getEntities();

    Entity getEntity(String entityName);

    void clearEntities();
}
```

1. a `Scene` have a title, a human thing to identify it,
2. `create` to build the `Scene`,
3. `initialize` to setup (or reset) everything in the `Scene`,
4. `input` to let's interact player with some scene object,
5. `update` to define specific update mechanism du to the `Scene`, e.g., managing menu component interaction,
6. `draw` anything about `Scene` required things but `Entity` standard update.
7. `dispose` at the end of the `Scene`, before switching to another `Scene`.
8. anything about managing internal new entities.

## SceneManager to rule'em all

As we now have a Scene interface, we need a manager to create, activate, deactivate and or close them.

```java
public static class SceneManager {
    // <1>
    private KarmaApp app;
    private Scene current;
    private Map<String, Scene> scenes = new HashMap<>();

    // <2>
    public SceneManager(KarmaApp app) {
        this.app = app;
    }

    // <3>
    public void add(Scene scene) {
        this.scenes.put(scene.getTitle(), scene);
    }

    // <4>
    public void start() {
        if (Optional.ofNullable(current).isEmpty()) {
            start("init");
        }
    }

    public void start(String sceneName) {
        if (Optional.ofNullable(current).isEmpty() || !current.getTitle().equals(sceneName)) {
            this.current = scenes.get(sceneName);
        }
        this.current.clearEntities();
        this.current.create(app);
        this.current.initialize(app);
    }

    // <5>
    public void activate(String name) {
        if (Optional.ofNullable(this.current).isPresent()) {
            this.current.dispose(app);
        }
        start(name);
    }

    public Scene getCurrent() {
        return this.current;
    }
} 
```

1. All the internal attributes, the current parent application, the current active scene, and the list of scenes.
2. A default constructor to initialize the internal parent app reference,
3. a way to add a scene to the manager,
4. two `Scene` starters: one with a default "init" one and another by providing the required scene,
5. this is the good way to activate a `Scene` on its name,

The `AbstractScene` will help us to support entities into a `Scene`.

## The AbstractScene to manage entities

The `AbstractScene` is an abstract class implementing partially the `Scene` interface and also support all the `Entity`
management operation for that `Scene`.

```java
public abstract class AbstractScene implements KarmaApp.Scene {
    //<1>
    private final Map<String, KarmaApp.Entity> entities = new ConcurrentHashMap<>();
    private final KarmaApp.World world;

    //<2>
    public AbstractScene(KarmaApp app) {
        this.world = app.getWorld();
    }

    //<3>
    protected void addEntity(KarmaApp.Entity e) {
        entities.put(e.name, e);
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

    // <4>
    public KarmaApp.World getWorld() {
        return this.world;
    }

}
```

1. the internal list of entities and the `World` object for this `Scene`,
2. the `World` object is by default initialized with parent application one,
3. everything about `Entity`, add, clear and getters,
4. retrieve the `Scene`'s `World` instance.

## Usage into KarmaApp

Now we defined some Scene interface, an Abstract layer to support Entity management, we can now implment SceneManager
usage into the KarmaApp class.

We can remove the entities and the world attributes from the `KarmaApp`.

And now initialize the `SceneManager`:

```java
private void init(String[] args) {
    //...
    sceneManager = new SceneManager(this);
    sceneManager.add(new TitleScene(this));
    sceneManager.add(new PlayScene(this));
}
```

For our demonstration purpose, we here create two new `Scene` implementations, a _title_ and a _play_ scenes.

And at first operation of the loop :

```java
private void loop() {
    sceneManager.start("title");
    //...
}
```

And on each of the 3 main loop operations in the `KarmaApp` class:

```java
public void input() {
    // <1>
    sceneManager.getCurrent().input(this);
}

public void update(long d) {
    // <2>
    Collection<Entity> entities = sceneManager.getCurrent().getEntities();
    //...
    // <3>
    sceneManager.getCurrent().update(this, d);
}

public void draw() {
    // prepare rendering pipeline
    Graphics2D g = buffer.createGraphics();
    //...
    // <4>
    Collection<Entity> entities = sceneManager.getCurrent().getEntities();
    //...
    // <5>
    sceneManager.getCurrent().draw(this, g);
    //...
}
```

1. Delegate input management to the current `Scene` implementation,
2. retrieve the current scene entities to update them all,
3. delegate specific update processing to the current active `Scene`,
4. Retrieve the current active `Scene` entities to draw them all,
5. delegate some specific supplementary drawing activities to the current active `Scene`.

## The Scenes

We are going to modify a little the play by creating two scenes:

- one to welcome the player a `TitleScene`
- and a second one with the game itself, the `PlayScene`.

### The title screen

### The game screen

TO BE CONTINUED ...