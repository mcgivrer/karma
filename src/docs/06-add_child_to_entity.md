# Adding child to Entity

In certain circumstances, having a group of entities could be a plus. When it's about animating some particles,
having a "group" of particles would greatly simplify their management.

And in terms of architecture, adding the Group notion is complex, and introducing another level of hierarchy through a
différent object may
drive us to an unhandy situation; let's try having some child!

## New child attribute

In the existing `Entity` class, we add a new _child_ attribute with some helpers to add a child `Entity`
or to get all the child entities.:

```java
public static class Entity {
    //...
    private Collection<Entity> child = new ArrayList<>();

    //...
    public Entity add(Entity c) {
        child.add(c);
        return this;
    }

    public Collection<Entity> getChild() {
        return child;
    }
    //...
}
```

And we must also introduce an update delegation :

```java
public void update(long d) {
    child.stream().forEach(c -> update(d));
}
```

## processing child Entities

As an Entity has a child, we must update the draw method to process those children!

```java
public void update(long d) {
    Collection<Entity> entities = sceneManager.getCurrent().getEntities();
    entities.stream()
            .filter(e -> !e.getPhysicType().equals(PhysicType.NONE))
            .sorted(Comparator.comparingInt(Entity::getPriority))
            .forEach(e -> {
                //...
                e.getChild().forEach(c -> applyPhysics(c, world, d));
                e.update(d);
            });
    sceneManager.getCurrent().update(this, d);
}
```

and the draw process :

```java
public void draw() {
    // prepare a rendering pipeline
    //...
    // Draw things
    Collection<Entity> entities = sceneManager.getCurrent().getEntities();
    entities.stream()
            .filter(Entity::isActive)
            .sorted(Comparator.comparingInt(Entity::getPriority))
            .forEach(e -> {
                // <1>
                draw(e, g);
                // <2>
                e.getChild().forEach(c -> draw(c, g));
            });
    sceneManager.getCurrent().draw(this, g);
    //...
}

// <3>
private void draw(Entity e, Graphics2D g) {
    switch (e.getClass().getSimpleName()) {
        case "TextObject" -> {
            drawTextObject((TextObject) e, g);
        }
        case "Entity" -> {
            drawEntity(e, g);
        }
        case "GridObject" -> {
            drawGridObject((GridObject) e, g);
        }
    }
}
```

1. delegate the draw entity process to a separated method (<3>),
2. parse all the children and draw them

There is also An impact on the `Scene#input()` where all the child `Entity` must be processed:

```java
public void input() {
    // <1>
    sceneManager.getCurrent().input(this);
    // <2> process all input behaviors
    sceneManager.getCurrent().getEntities().stream()
            .filter(KarmaApp.Entity::isActive)
            .forEach(e -> {
                processInput(e);
            });
}

public void processInput(Entity e) {
    // <3> apply entity's behaviors
    if (!e.getBehaviors().isEmpty()) {
        e.getBehaviors().forEach(b -> {
            b.onInput(this, e);
        });
    }
    // <4> apply child's behaviors.
    e.getChild().forEach(c -> processInput(c));
}
```

1. process the possble `Scene` specific input,
2. parse all the entities in the active scene and ask for processing Entity,
3. Process all the `Behavior#inInput` for entities,
4. and apply t esame processing for all child entities.

> **Note for the Future**<br/>
> The _child_ attribute will be useful as soon as we will implement some particles where each particle
> will share some common behaviors. A specific `Behavior` will be set on the parent `Entity` object having all
> the child `Entity` acting as particles, and this `Behavior` will be applied on all of them.
