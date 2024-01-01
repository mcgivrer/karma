## Bash Script Documentation

### Overview

This script is designed to compile, package, and manage a Java project. It provides options for generating Javadocs,
running tests, checking code quality against style rules, wrapping JARs, and creating documentation in EPUB and PDF
formats.

### Setup

The script sets the environment and reads properties from a specified properties file to configure the build
environment.

```bash
ENV=build
```

Function `prop` is used to read properties from the `${ENV}.properties` file.

### Exports

Various environment variables are set using the properties file, such
as `PROGRAM_NAME`, `PROGRAM_VERSION`, `MAIN_CLASS`, `JAVADOC_CLASSPATH`, and more.

### Paths

File paths for source, libraries, target, and other build directories are defined.

### Functions

The script contains multiple functions to perform different tasks:

1. `manifest()`: Creates the Manifest file for the JAR.
2. `compile()`: Compiles the Java sources.
3. `checkCodeStyleQA()`: Checks code quality against specified rules.
4. `generatedoc()`: Generates Javadoc documentation.
5. `generateSourceJar()`: Creates a JAR of the source files.
6. `executeTests()`: Compiles and executes JUnit tests.
7. `createJar()`: Packages the compiled classes into a JAR file.
8. `wrapJar()`: Wraps the JAR as a shell script.
9. `executeJar()`: Executes the built JAR file.
10. `generateEpub()`: Generates documentation in EPUB format.
11. `generatePDF()`: Generates documentation in PDF format.
12. `sign()`: Placeholder for a function to sign the JAR (not yet implemented).
13. `help()`: Displays usage help for the script.

### Execution

The `run` function contains the main logic for executing the build process. Depending on the command-line argument
passed to the script, different functions are executed. For example, passing "all" as an argument will execute almost
all the defined functions, while passing "compile" will only compile the Java sources.

Available command line options include:

- `a|A|all`: Perform all operations.
- `c|C|compile`: Compile all project sources.
- `d|D|doc`: Generate Javadoc for the project.
- `e|E|epub`: Generate an EPUB file as documentation.
- `k|K|check`: Check code quality against a set of rules.
- `t|T|test`: Execute JUnit tests.
- `j|J|jar`: Build a JAR with all resources.
- `w|W|wrap`: Build and wrap the JAR as a shell script.
- `p|P|pdf`: Generate a PDF file as documentation.
- `s|S|sign`: Build and wrap a signed JAR as a shell script (not implemented yet).
- `r|R|run`: Execute (and build if needed) the created JAR.
- `h|H|?`: Display help.

### Properties File Documentation

here is a Sample properties file:

```properties
project.name=sjdp
project.title=SJDP
project.version=0.0.1
project.main.class=com.snapgames.core.App
project.javadoc.classpath=com.snapgames
project.javadoc.packages=-group "Core package" com.snapgames.core com.snapgames.core.entity com.snapgames.core.gfx com.snapgames.core.io com.snapgames.core.loop com.snapgames.core.physic com.snapgames.core.scene com.snapgames.core.service com.snapgames.core.utils -group "Demo Package" com.snapgames.demo.test001.scenes
project.author.name=Frédéric Delorme
project.author.email=frederic.delorme@gmail.com
project.build.jdk.version=20
```


where :

#### `project.name`

- **Value**: `sjdp`
- **Description**: The unique identifier or short name of the project.

#### `project.title`

- **Value**: `SJDP`
- **Description**: The title or full name of the project, which can be used for display purposes.

#### `project.version`

- **Value**: `0.0.1`
- **Description**: Specifies the current version of the project.

#### `project.main.class`

- **Value**: `com.snapgames.core.App`
- **Description**: The main entry point class for the project, which contains the `main` method.

#### `project.javadoc.classpath`

- **Value**: `com.snapgames`
- **Description**: Specifies the base classpath for generating JavaDoc documentation.

#### `project.javadoc.packages`

- **Value**: Various package groupings
- **Description**: Defines how JavaDoc should group the classes during documentation. For example, classes
  in `com.snapgames.core` and related subpackages are grouped under "Core package".

#### `project.author.name`

- **Value**: `Firstname Lastname`
- **Description**: The name of the primary author or maintainer of the project.

#### `project.author.email`

- **Value**: `firstnameDOTlastnameATmailDOTcom`
- **Description**: The email address of the primary author or maintainer of the project, for contact purposes.

#### `project.build.jdk.version`

- **Value**: `20`
- **Description**: Specifies the version of the JDK used for building the project.

---

This documentation provides an overview of the keys and values present in the properties file and can be included in the
user or developer documentation for better clarity.

---




_**Note:** The above documentation provides a high-level overview of the script's functionality. Some details
might need to be expanded upon depending on the audience's familiarity with the script and the build process._
