# Add platforms

To satisfy the goal of that project, we need to get some entities interacts with some platforms.

So, go and add some Entity with `PhysicType.STATIC` to the playground.

```text
+-P1--------------------------------------+
|  00000                        ══════ ❤︎5|
+-+-------------------------------------+-+
P2|       +P5-----+                     P3|
| |       |       |                     | |
| |       +-------+                     | |
| |                                     | |
| |                                     | |
+P4-------------------------------------+-+
|                                         |
+-----------------------------------------+
```

Goto the PLayScene and add new entities :

```java
public class PlayScene extends AbsytryactScene {
    //...
    public void create(KarmaApp app){
        //...
        createPlatforms(app);
        //...
    }
    //...

    private void createPlatforms(KarmaApp app) {
        KarmaApp.Entity platform1 = new KarmaApp.Entity("platform_01")
                .setPosition(100, 100)
                .setSize(100, 32)
                .setPhysicType(KarmaApp.PhysicType.STATIC)
                .setType(KarmaApp.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(new KarmaApp.Material(1.0, 1.0, 0.1));
        addEntity(platform1);

        KarmaApp.Entity platform2 = new KarmaApp.Entity("platform_border_top")
                .setPosition(0, 0)
                .setSize((int) app.getWorld().getPlayArea().getWidth(), 16)
                .setPhysicType(KarmaApp.PhysicType.STATIC)
                .setType(KarmaApp.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(new KarmaApp.Material(1.0, 1.0, 0.1));
        addEntity(platform2);

        KarmaApp.Entity platform3 = new KarmaApp.Entity("platform_border_bottom")
                .setPosition(0, getWorld().getPlayArea().getHeight())
                .setSize((int) getWorld().getPlayArea().getWidth(), 16)
                .setPhysicType(KarmaApp.PhysicType.STATIC)
                .setType(KarmaApp.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(new KarmaApp.Material(1.0, 1.0, 0.35));
        addEntity(platform3);

        KarmaApp.Entity platform4 = new KarmaApp.Entity("platform_border_left")
                .setPosition(0, 16)
                .setSize(16, (int) getWorld().getPlayArea().getHeight())
                .setPhysicType(KarmaApp.PhysicType.STATIC)
                .setType(KarmaApp.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(new KarmaApp.Material(1.0, 1.0, 0.35));
        addEntity(platform4);

        KarmaApp.Entity platform5 = new KarmaApp.Entity("platform_border_right")
                .setPosition((int) getWorld().getPlayArea().getWidth() - 16, 16)
                .setSize(16, (int) getWorld().getPlayArea().getHeight()-16)
                .setPhysicType(KarmaApp.PhysicType.STATIC)
                .setType(KarmaApp.EntityType.RECTANGLE)
                .setMass(4.0)
                .setBorderColor(Color.GRAY)
                .setBackgroundColor(Color.DARK_GRAY)
                .setPriority(10)
                .setMaterial(new KarmaApp.Material(1.0, 1.0, 0.35));
        addEntity(platform5);

    }
    //...
}
```

Here we now have a new bunch of platform entities to interact with:

![Adding some platforms](illustrations/08-add_platform-01.png)

_figure 8.1 - Adding some platforms to the game_

## Enhancing the collision detection and resolution

Now we have platform, we need to know on wich side the of an entity the collision happened to run the correct behavior according to that collision.

Here is the right opportunity to create some `CollisionEvent`.

This `CollisionEvent` will provide :

- `srcCollision` the entity colliding,
- `dstCollision` the entity colliding with,
- `collisionNormal` the normal vector for that collision,
- `penetrationDepth` the penetration depth of this collision,
- `side:CollisionSide` the side of the collision on the srcEntity.

So the corresponding class will be:

```java
public static class CollisionEvent{
    private Entity srcCollision;
    private Entity dstCollision;
    private Vector2D collisionNormal;
    private double penetrationDepth;
    private CollisionSide side;
}
```

And the required ENUM for the side attribute:

```java
public enum CollisionSide {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT;
}
```

### Modifying the existing algorithm
