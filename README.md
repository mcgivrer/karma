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

![The UML class diagram](http://www.plantuml.com/plantuml/png/VL5DRzD04BtlhrXo2a91uhe7LAe28W5HgPKu80vZxyHUs0zhPao8KFyxwxLfRKVgnNxpvit7lBrY0P8-UFL2MWoeEmykgjPPr06bJPR704J8LRqWAzL1_GiQrFkRmr-bjOk2T8GQzG7FdnqBHgG5lx-5Y5uzgDe3ipu01RZr8CT4GUEVN61vwFPw56eNEey1eupihEqmxrLD289tTIQsBmV2nQwN4VQTpRcjDJx5fZ2Mu5HoEnUTR84QDdT2BZRVVnJQEkuIuwZnANKPFIXkqscrwMI8LXZxeDz7SNA-FtSudZKHK_0jaRSJ_aZEVgr-OYtJiIljVMQVozyubfugY3Mk1KyUNsCBlrsY6LKScdeK_0KYD4WJzQvY8QYbfEQUB-fI7-t_EhKisRpPwDtkhTx4pRIFmU7da5VAB2g6c5EnfU1IEZlDs2VCbvEoEizvFDDK4s0rZYxRv8xFYh9Wzcv6yaSjtpWBhllZ4Gdjfv2FDbM9BbLw_u4We3aEttKwv-U66vu5bzg5oOONZi_R51kA2uKTkxAwmcZxuFy1 "The full UML Diagram for the KarmaPlatform")

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

Enjoy !

Frédéric Delorme.
