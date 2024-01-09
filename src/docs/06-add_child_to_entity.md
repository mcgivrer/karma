# Adding child to Entity

In certain circumstances, having a group of entities could be a plus. When it's about animating some particles, 
having a "group" of particles would greatly simplify their management.

And in terms of architecture, adding the Group notion is complex, and introducing another level of hierarchy through a différent object may
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
                draw(e,g);
                // <2>
                e.getChild().forEach(c->draw(c,g));
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

