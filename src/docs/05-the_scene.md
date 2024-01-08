# The Scene

When it's come to build a game, there is multiple steps im its scenary.
You start the game, and it appears the Title screen, then you navigate to some menu to start
the adventure, the game screen opens, and you play sometime, until you lost or were killed inm the game.

Each of those states contains different play type. If you want to implement all of these, the complexity may
raise too much.

The best way to solve this complexity is to split each level into sub programs.

This is where,like in a movie, we will use some scenes !

## Scene interface

Now we have a way to go,let's standardize the way we will implement a state : a `Scene`.

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
}
```

1. a `Scene` have a title, a human thing to identify it,
2. `create` to build the `Scene`,
3. `initialize` to setup (or reset) everything in the `Scene`,
4. `input` to let's interact player with some scene object,
5. `update` to define specific update mechanism du to the `Scene`, e.g. managing menu component interaction,
6. `draw` anything about `Scene` required things but `Entity` standard update.
7. `dispose` at the end of the `Scene`, bedfore switching to another `Scene`.

## SceneManager to rule'em all

As we mow have a Scene interface, we need a manager to create, activate, deactivate and or close them.

```java
public static class SceneManager {
    private KarmaApp app;
    private Map<String, Scene> scenes = new HashMap<>();

    public SceneManager(KarmaApp app) {
        this.app = app;
    }

    public void add(Scene scene) {
        this.scenes.put(scene.getTitle(), scene);
    }
} 
```

