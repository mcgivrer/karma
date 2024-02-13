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

![The UML class diagram](http://www.plantuml.com/plantuml/png/VPBFZjCm4CRlVWhJKn4g4bSVqB9QGBGWAkrA7127IJn93FuJUIQK2ksxuzYfDadHNjAPRtupOt_k3J547fmLBxX3XxAtOBpeZ4RfaBkWgIJ074qrC98GFTI_e4Mvlply5L8QpnWRg54UyFpP4AF7kD3tH_1enQEeBH390qG7HmlSXEWa_aaDciPfhmieZUijElHSrDdOST-hEY8mtjT9sBuS1UFxWKiuz3hLjbhPuDkiw0YdNDiPRtWBiILrpz7uzlkFB6j3VI0iZ0sXJz6Ze2wSLYUT3942_U3aUy-6pm_d7ikr9c5AleLezHH_Z4P_hNvYpTDbL-n3Kf-htndY8LRWQrm3JnxVOWU_JOWpwGYHJMtnKheq_u_DRfPz9Vu27bgCa_Ik8evGSoxDtsVH5mQlBtrYsR9viv6xtLkvyPlfx0h77uEq8XEKpLEdR66ES-lC1gMVOhhUbEXnp-MQgfe24cNro8nxdeKIC7jZnUaZbl-71DmEJOCHzIS7BQegPzUKt7-8u50rutTTJlMvuOfcoJMxC5dHml7vc6n3TY6hPIg96_HwSFOV "The full UML Diagram for the KarmaPlatform")

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

![The latest screenshot from the karma platform](src/docs/illustrations/00-latest.png "This is the latest screenshot from the Karma Platform")

_figure 2 - the latest build of the Karna Platform_

## Contribute

You can change the source code freely and add/or new build dependencies through the `build/properties` file, see
the [build.readme.md](./build.readme.md) file for details.

![RepoBeats analytics](https://repobeats.axiom.co/api/embed/3700d019258205e1470117ea5f5d4b870d704ce0.svg "Repobeats analytics image")

Enjoy !

Frédéric Delorme.
