# README

## Context

This is the readme file for **Karma (1.0.4)** project.
A game oriented java project proposing basic implementation for

- `Entity` as game objects,
- Physic engine computation based on a `World` context
- and key input processing.
- Some `Behavior` can be added to the `Entity` to process some common behaviors.

## Description

The project is composed of one main class `KarmaPlatform` that contains subclasses.

```plantuml
@startuml
!theme plain
hide methods
hide attributes

class KarmaPlatform extends JPanel implements KeyListener{
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
show KarmaPlatform methods 
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
KarmaPlatform --> BufferedImage:buffer
KarmaPlatform -->  JFrame:frame
KarmaPlatform --> SceneManager:sceneManager
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

![The RepoBeats statistics](https://repobeats.axiom.co/api/embed/fba6f0b8b964550bc9c1dd5af0d4a20f5f7edd39.svg "Repobeats analytics image")

Enjoy !

Frédéric Delorme.
