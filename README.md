# README

## Context

This is the readme file for **Karma (1.0.0)** project.
A game oriented java project proposing basic implementation for

- `Entity` as game objects,
- Physic engine computation based on a `World` context
- and key input processing.
- Some `Behavior` can be added to the `Entity` to process some common behaviors.

## Description

The project is composed of one main class `KarmaApp` that contains subclasses.

```plantuml
@startuml
!theme plain
hide methods
hide attributes

class KarmaApp extends JPanel implements KeyListener{
  +createScene()
  +input()
  +update(d:long)
  +draw()
  -init(args:String[])
  -dispose()
  -loop()
}
package JDK{
}
show KarmaApp methods 
enum EntityType
class Entity
class World
class GridObject extends Entity
class TextObject extends Entity
interface Behavior
interface Scene
class SceneManager
class AbstractScene implements Scene

AbstractScene "1" --> "n" Entity:entities
AbstractScene -> World:world
AbstractScene "1"-->"n" Behavior:behaviors
Entity "1" -> "n" Behavior:behaviors
Entity -> EntityType:type
KarmaApp --> BufferedImage:buffer
KarmaApp -->  JFrame:frame
KarmaApp --> SceneManager:sceneManager
SceneManager "1"->"n" Scene:scenes

@enduml
```

## Build

To build the project, execute the following command line :

```bash
./build.sh a
```

## Run

To execute the build project, run it with :

```bash
./build.sh r
```

you can execute the command line :

```bash
java -jar target/Karma-1.0.0.jar
```

or on linux machine, or git-bash on Windows :

```bash
target/build/Karma-1.0.0.run
```

## Contribute

You can change the source code freely and add/or new build dependencies through the `build/properties` file, see the [build.readme.md](./build.readme.md) file for details.

Enjoy !

Frédéric Delorme.
