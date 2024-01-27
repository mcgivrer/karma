# World's Disturbance the force

## What is a Disturbance

A Disturbance is a contextual element that influences the global environment where objects are moving.

- The _wind_ is a disturbance, applying constant force as a 2D vector,
- the _water_ is another element of context that influences
  the global behavior of all objects under that disturbance.

Our `Disturbance` class is another thing more than an `Entity` but without drawing part, until you decide to draw
it through a specific implementation of a Behavior.

### the Disturbance class

So the class is inherited from Entity and set some attributes on default values:

1. the default entity type is set to a RECTANGLE, and the physic type is set to NONE,
2. the border and fill colors are unset to remove drawing capability.

```java
public static class Disturbance extends Entity {
    public Disturbance(String name) {
        super(name);
        // <1>
        setPhysicType(NONE);
        setType(RECTANGLE);
        // <2>
        setBackgroundColor(null);
        setForegroundColor(null);
    }
}
```

### World have some disturbances

And add the `Disturbance` to the `World` class:

```java
public static class World {

        private Rectangle2D playArea;
        private double gravity;
        //<1>
        private final List<Disturbance> disturbances = new ArrayList<>();
        //...
        //<2>
        public World addPerturbation(Disturbance p) {
            disturbances.add(p);
            return this;
        }
        //<3>
        public List<Disturbance> getDisturbances() {
            return disturbances;
        }
    }
```

1. Adding the list of `Disturbance`,
2. method to add a `Disturbance` to the `World`,
3. getting all the `Disturbance` list from the `World`.

### Integrate Disturbance in the physic computation

then, our `Entity` must be moving according to the new `Disturbance` from the `World`, we will modify 
the `KarmaPlatform#update()` :

```java
public class KarmaPlatform{
    //...
    public void update(double d) {
        //...
        entities.stream()
                .filter(Entity::isActive)
                .forEach(e -> {
                    if (!e.getPhysicType().equals(PhysicType.NONE)) {
                        // if concerned, apply World disturbances.
                        applyWorldDisturbance(world, e, d);
                        // compute physic on the Entity (velocity & position)
                        applyPhysics(world, e, d);
                        //...
                    }
                });
        //...
        }
    //...
}
```

So the new `applyWorldDisturbance()` is as below :

```java
private void applyWorldDisturbance(World world, Entity entity, double d) {
    for (Disturbance dist : world.disturbances) {
        if (dist.box.intersects(entity.box) || dist.box.contains(entity.box)) {
            entity.forces.addAll(dist.forces);
        }
    }
}
```
So before computing physique for each entity, the intersecting/containing Disturbance force is applied on.

### Displaying for debug purpose

Ok, by default wind is not visible (as soon as no feather are swiped away by), for debug purpose (and also for fun), we
will add a way to display a transparent rectangle showing the wind area.

```java
//...
private void draw(Graphics2D g, Entity e) {
    switch (e.getClass().getSimpleName()) {
        //...
        case "Disturbance" -> {
            drawDisturbance(g, (Disturbance) e);
        }
    }
    //...
}
//...
```
And the corresponding method:

```java
private void drawDisturbance(Graphics2D g, Disturbance e) {
    if (isDebugGreaterThan(3)) {
        if (Optional.ofNullable(e.getBackgroundColor()).isPresent()) {
            g.setColor(e.getBackgroundColor());
        } else {
            g.setColor(new Color(0.0f, 0.0f, 0.6f, 0.3f));
        }
        g.fillRect((int) e.getPosition().getX(), (int) e.getPosition().getY(), (int) e.w, (int) e.h);
    }
}
```

>**NOTE** We will use a default color if none are already defined.

## Wind simulation

To simulate a wind, it is nothing more than adding a force vector on all entity crossing a specific area in the play 
area. we will first start by adding such thing to our demo.

```java
import my.karma.app.AbstractScene;

public class PlayScene extends AbstractScene{
    //...
    @Override
    public void create(KarmaPlatform app) {
        KarmaPlatform.World w = getWorld();
        w.addPerturbation((KarmaPlatform.Disturbance)
                new KarmaPlatform.Disturbance("wind")
                        .setPosition(0, 0)
                        .setSize(w.getPlayArea().getWidth(), w.getPlayArea().getHeight() * 0.8)
                        .addForce(new KarmaPlatform.Vector2D(0.002, 0.0))
        );
        //...
    }    
    //...
}
 ```

Then running this new demo will drive all the Entity contained or intersecting by this Disturbance to be moved 
thanks to the added force. You will see all the generated objects moving to the right.

adding some debug



## Water simulation target

To simulate an object floating on water in a Java game physics engine, you can use Archimedes' buoyancy force and
friction forces. Here are the general steps to implement this simulation:

1. Determine the mass of the floating object and the density of the water.
2. Calculate the submerged volume of the object using its mass and the density of water. The formula is: volume = mass /
   water density.
3. Calculate the Archimedes' buoyancy force using the formula: buoyancy force = volume * water density * gravity, where
   gravity is the acceleration due to gravity.
4. Apply the Archimedes' buoyancy force to the floating object in the direction opposite to gravity.
5. Calculate the friction force between the object and the water to simulate resistance to its movement. You can use
   empirical formulas or physical models for this.
6. Add the friction force to the simulation to slow down the object's movement.
7. Repeat these steps at each simulation update to maintain the object's buoyancy.

It is important to note that this answer provides a general approach to simulate the buoyancy of an object in a Java
game physics engine. The implementation details may vary depending on the physics library used in the specific game
engine.

To be able to do this, we need to change the physic model and add Forces and acceleration to the `Entity`, to be
computed by the physic computation process.

