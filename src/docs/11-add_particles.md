# Add particle behavior

While we already add `Behavior` to our `Entity` and add child's Entity list to any `Entity`, we are ready to go further
with new animation possibilities: add particle behaviors.

## What is a particle?

A _particle_ is the smallest element from a particle system that be animated by a common behavior shared bay all the
particles of a system.

In our case, the particle system will be an `Entity`,and the particles of that system will be its child.

Basically, the particle is created, lives a certain period of time, and then disappears.

In our Entity, we already have the base mechanism to manage the period of time the entity is active:
the duration attribute.

To be able to create a particle from an Entity, we are just missing a specific `Behavior.onCreate()` to define the
right operation to create a new particle in the system.

## The ParticleBehavior interface

As described before, we are missing the onCreate API, so here it is :

<figure>
<img src="https://www.plantuml.com/plantuml/svg/VP1DSvf06CVlV0fwIwOX2X4q73enWXhnFOBWXLbWTJSk2xAxWDFfTwyp6ZkjRM_Fyyp_PN_xmZWek4Y9z8KZc48v9m1JAIQ0CTcc7FEZX702BxESGewoXFrs0vmNE18SCWbJ3eijYA7y2X4eSLPyb-IlSaRTF04StW7J0KKAvWJmRLQa3p8qpo4FSc8ccOW8l3yRtcWk-B_r5vLLWEfMj3C71SWHZfbctLXw6I6OuOpUnT2yBdO9AR-NVfp1FX__TxGvA3YE2RogOCqXJTYVpBq2_fVvMlShyBRsyiUAykqQRqQNWKalfrwH4iHvpin6ewggnvm0oa-tnpXB6vzBWvMxXZTJw6ZTeq-T9U-tfhF9Hsk_KQF1S8nzQdLrDMn6hx7t_EvKjdFGEYlcYcfTqdN0XeI4OUYotNgCNckZmbHGPQQf52pqYKS76uKtWP_iZA0lLBVs1mSdJ9QS3QFCKCK0QJqM62rXhm9bOT6y6LjvfOzrrDwhuccKTmvAkG-TjQj_PDE3uOknloq3CiAuGuWsUfvqaTOrggbzXDPcuIrZzv1aHpMt8-GdapQ3YTFjTsozBPP1i3qrkzsmHlqUj_ntCczvqsNj5KwxHWZMSFKMx-OaRJL_0W00"
alt="UML class diagram showing the Behavior and ParticleBehavior inheritance" title="ParticleBehavior interface inheriting from the Behavior"/>
<figcaption>figure 11.1 - The <code>ParticleBehavior</code> interface inheriting from the <code>Behavior</code> already existing interface</figcaption></figure>

Here is the possible interface definition:

```java
public interface ParticleBehavior<Entity> extends Behavior<Entity> {
    default void onCreate(KarmaPlatform a, Entity e) {
    }
}
```

It is time for a simple first implementation: a background star field.

## A star field particle system

What is a star field?

A star field is a bunch of stars in a dynamic sky, moving accordingly to the opposite of the Camera moves,
on some multiple parallax.

Those parallax will be some computation based on the priority of each child Entity multiplied by a specific factor.

The `StarFieldParticleBehavior` is the implementation for the Star animation.

```java
public class StarFieldParticleBehavior implements KarmaPlatform.ParticleBehavior<KarmaPlatform.Entity> {
    private int nbStars;
    private KarmaPlatform.Entity focusEntity;
    private double speedRatio;
    private double starSpread;
}
```

The class contains the following attributes :

- `nbStars` the number of stars to be displayed on the play area,
- `focusEntity` is the `Entity` focused as a target of the star field movement,
- `speedRatio` the ratio to be applied on the target velocity,
- `starSpread` this value is the max used to dispatch child particles around the parent `Entity` priority value.

Those parameters are defined through the constructor:

```java
public class StarFieldParticleBehavior implements KarmaPlatform.ParticleBehavior<KarmaPlatform.Entity> {
    //...
    // <1>
    public StarFieldParticleBehavior(KarmaPlatform.Entity focusEntity, double speedRatio, int nbStars, double starSpread) {
        this.speedRatio = speedRatio;
        this.focusEntity = focusEntity;
        this.nbStars = nbStars;
        this.starSpread = starSpread;
    }

    // <2>
    public StarFieldParticleBehavior() {
        speedRatio = 0.001;
        starSpread = 30;
        nbStars = 20;
    }
}
```

With the parameterized constructor `<1>` you can define each values, or use default one with the second
constructor `<2>` without parameter.

then the implemented methods are:

### onCreate

```java
public class StarFieldParticleBehavior implements KarmaPlatform.ParticleBehavior<KarmaPlatform.Entity> {
    //...
    @Override
    public void onCreate(KarmaPlatform a, KarmaPlatform.Entity e) {
        for (int i = 0; i < nbStars; i++) {
            // <1>
            KarmaPlatform.Entity particle = new KarmaPlatform.Entity(e.name + "_P" + e.id)
                    .setPosition(
                            a.getWorld().getPlayArea().getWidth() * Math.random(),
                            a.getWorld().getPlayArea().getHeight() * Math.random())
                    .setSize(0.5, 0.5)
                    .setPhysicType(KarmaPlatform.PhysicType.NONE)
                    .setStatic(true)
                    .setMass(1.0)
                    .setForegroundColor(new Color(1.0f, 1.0f, 1.0f, 0.5f))
                    .setBackgroundColor(new Color(1.0f, 1.0f, 1.0f, 0.5f))
                    .setPriority(e.getPriority() + (int) (Math.random() * starSpread) + 1);

            // <2>
            e.getChild().add(particle);
            // <3>
            if (!e.box.contains(particle.box)) {
                e.box.add(particle.box);
            }
            // <4>
            e.setSize(e.box.getWidth(), e.box.getHeight());
        }

    }
    //...
}
```

- We create a bunch of particles `<1>`,
- then it is added `<2>` to as child entity to the `Entity` have this `StarFieldParticleBehavior`
- we adapt the parent bounding box `<3>` with the newly added particle,
- and finally, we fix the Entity size `<4>` with the size of the resulting bounding box size.

### onUpdate

Then oin each update, we are going to move each star along the focused Entity, according to its position and velocity:

```java
public class StarFieldParticleBehavior implements KarmaPlatform.ParticleBehavior<KarmaPlatform.Entity> {
    //...
    @Override
    public void onUpdate(KarmaPlatform a, KarmaPlatform.Entity e, double d) {
        // <1>
        if (Optional.ofNullable(focusEntity).isEmpty()) {
            focusEntity = a.getSceneManager().getCurrent().getEntity("player");
            // <2>
            if (e.getChild().size() < 200) {
                this.onCreate(a, e);
            }

            for (KarmaPlatform.Entity c : e.getChild()) {
                double dx = -focusEntity.getVelocity().x;
                double dy = -focusEntity.getVelocity().y;
                // <3>
                c.setPosition(
                        c.getPosition().x + (speedRatio * dx * c.getPriority()),
                        c.getPosition().y + (speedRatio * dy * c.getPriority())
                );
                // <4>
                if (!a.getWorld().getPlayArea().contains(c.box)) {
                    if (c.getPosition().x > a.getWorld().getPlayArea().getWidth()) {
                        c.getPosition().x = 0;
                    }
                    if (c.getPosition().x < 0) {
                        c.getPosition().x = a.getWorld().getPlayArea().getWidth();
                    }
                    if (c.getPosition().y > a.getWorld().getPlayArea().getHeight()) {
                        c.getPosition().y = 0;
                    }
                    if (c.getPosition().y < 0) {
                        c.getPosition().y = a.getWorld().getPlayArea().getHeight();
                    }
                }
            }
        }
    }
}
```

1. if there is no identified focus Entity, we try to get the "player" named one. if not nothing will happen.
2. we check if there is less than 200 stars, we create ore by calling the `onCreate` method,
3. we defined the new star position based on its previous position, the focus entity velocity and the particle system
   speed ratio,
4. and we check that the moved star remains in the play area.

### onDraw

To draw faster as possible all those star, we use the Graphics2D API directly from the onDraw method:

```java
public class StarFieldParticleBehavior implements KarmaPlatform.ParticleBehavior<KarmaPlatform.Entity> {
    //...
    @Override
    public void onDraw(KarmaPlatform a, Graphics2D g, KarmaPlatform.Entity e) {
        e.getChild().forEach(c -> {
            g.setColor(c.getBackgroundColor());
            g.drawRect((int) c.box.getWidth(), (int) c.box.getHeight(),
                    (int) c.size.getX(), (int) c.size.getY());
        });
    }

}
```

And that's it !

<figure>
<img src="illustrations/08-add_particles-01.png" alt="a screenshot of the main PlayScene integrating some star field" title="Some stars in the background"/>
<figCaption>figure 11.2 - Some stars in the background</figCaption>
</figure>