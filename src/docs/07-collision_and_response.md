# Collision and Response

## Collision detection

- Using a space partitioning algorithm to dispatch all the Scene `Entity` list into some space cell, according to a
  limited number of nodes per cell, and a limited level of depth into the partition tree. The selected pattern is a
  Quadtree.
- Detect collision between `STATIC` and `DYNAMIC` PhysicType's Entity (`NONE` are excluded).
- On collision process response by calling the `Behavior#onCollide(CollisionEvent)` method for each concerned `Entity`.

### Entity update

The `Entity` class is updated with some new attributes :

- `collisions`:  a list of CollisionEvent.
-

```java
public static class Entity {
    //...
    private final Collection<CollisionEvent> collisions = new ArrayList<>();

    public Entity add(CollisionEvent ce) {
        collisions.add(ce);
        return this;
    }

    public Collection<CollisionEvent> getCollisions() {
        return collisions;
    }
    //...
}
```

### Behavior update

The existing Behavior interface is updated with a new event;: onCollide(CollisionEvent):

```java
public interface Behavior<Entity> {
    //...
    default void onCollision(CollisionEvent ce) {

    }
}
```

### CollisionEvent

We need a new object to define a collision: the `CollisionEvent` containing src and target of the collision, the
collision normal, the depth of penetration factor and a collision side:

```java
public static class CollisionEvent {
    private final Entity srcCollision;
    private final Entity dstCollision;
    private Vector2D collisionNormal;
    private double penetrationDepth;
    private CollisionSide side;
    // getters and setters.
}
```

## Collision Detection

On each detected collision, a `CollisionEvent` is created and dispatch to concerned `Entity`'s.

```java
public class KaraPlatform {
    //...
    public void update(long d) {
        Collection<Entity> entities = sceneManager.getCurrent().getEntities();
        // <1>
        cullingProcess(this, d);
        entities.stream()
                .filter(Entity::isActive)
                .forEach(e -> {
                    if (!e.getPhysicType().equals(PhysicType.NONE)) {
                        // compute physic on the Entity (velocity & position)
                        applyPhysics(world, e, d);
                        // <2> detect collision and apply response
                        detectCollision(world, e, d);
                        //...
                    }
                });
        //...
    }

    //...
    public synchronized void cullingProcess(KarmaPlatform game, double d) {
        spacePartition.clear();
        sceneManager.getCurrent().getEntities().stream()
                .filter(Entity::isActive)
                .forEach(e -> {
                    spacePartition.insert(e);
                });
    }
}
```

1. Modify the `KarmaPlatform#update` method to manage the Space partitioning
2. Let's detect possible collision

3. And the detection process itself by checking only the concerned space partition and the contained Entities :

```java
  private void detectCollision(World w, Entity e, double d) {
    Collection<Entity> entities = sceneManager.getCurrent().getEntities();
    List<Entity> collisionList = new CopyOnWriteArrayList<>();
    // <3>
    spacePartition.find(collisionList, e);
    collisionCounter = 0;
    e.clearCollisions();
    collisionList.forEach(o -> {
        if (e.isActive() && !o.equals(e) && o.isActive()
                && !o.getPhysicType().equals(PhysicType.NONE)) {
            collisionCounter++;
            handleCollision(e, o);
        }
    });
}

```

The handling of the resulting collision is processed into the `handleCollision` method.

## Collision Response

TODO

- capture and process event
- update Entity position
