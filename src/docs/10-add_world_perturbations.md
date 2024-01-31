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

