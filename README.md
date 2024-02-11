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

![The UML class diagram](https://www.plantuml.com/plantuml/png/VL7BZXCn4BpxAqnEGIX8N7r0oug7I42ij2jnG1nwnZqp1Zz6xHv2XFRVySE2IOYuEEsgIhchvYOo42_EYYSyeKCvMp1UJ4QZTCXJqB5UW9bCjp168MRelyE8Sl_wy4j8QJmZ3T2ZFE3fWucC7kaCtz-1HoiUHMyXHda0Sd1dWOT0Jk9FHgzZTdjUXTAusQ93pnLTsURtdlM4m7ZV9s3xD0F6pmlNSPbruhPQsU37WcY2O-5snXlU0erHtJCPFtxvMc1juXmYLeqDOKxJeuXJE5visWgH0ltYv1lFXayFfnbRh0gqo-T0LhVv7HdzgVk6FRUm5zg7X5xd_rPuYnFyC85MK2cWMjJv8tWOaHhqgejCq7EXrfNM_ubpVlDY8tUxbtBZD-rnXVd7OBnG9b49fuxv_DSckIIJFmkhhWrHLD-ioIR_5IN1gc35kUMpZr6MlLs60GdrUvTogwxShiZa_Ys1GpNaymg_Ba_5TPDhfcGhqGfQXL686_HwSVOt "The full UML Diagram for the KarmaPlatform")

_figure 1 - The full UML Diagram for the KarmaPlatform_

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

You can change the source code freely and add/or new build dependencies through the `build/properties` file, see
the [build.readme.md](./build.readme.md) file for details.

Enjoy !

Frédéric Delorme.
