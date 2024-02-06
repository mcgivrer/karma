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

To be able to create a particle from an Entity, we are just missing a specific `Bheavior.onCreate()` to define the
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

It is time for a simple first implementation: a background starfield.


## A star field particle system

What is a star field?

A star field is a bunch of stars in a dynamic sky, moving accordingly to the opposite of the Camera moves, 
on some multiple parallax.

!! TODO add a diagram there


