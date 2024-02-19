# A better configuration service

## Context

We already defined a `Configuration` class in our game, but adding a new parameter will ask us a lot of code.

Why not implementing a better and highly reliable configuration service, where adding some new parameters will only be
a breeze?

This is exactly what the proposed solution below will answer most of that thing using a simple enumeration.

## Parameter ?

What a configuration parameter is?

A parameter is a key value to be defined to change one of the application's behavior.
A perfect parameter will be a value of atype, a name for that parameter, a description, and if no value is set, a
default
value:

- _name_: the name of the parameter,
- _description_: what this parameter consists in,
- _value_: the desired value, ideally pict-up from configuration file or from CLI argument,
- _defaultValue_: the default value if no value is set on CLI or in configuration file.

And this parameter value will also have a type, defined by the way we "extract" the value from the _Command Line
Interface_ parameter of from the configuration file.
Most of the time, it is a String _parsing_ operation to convert the resulting value to a defined Type like boolean,
integer, double, or any other class the configuration may need of.

so our typical parameter can be :

```java
public interface ConfigAttribute<T> {
    String getName();

    T getValue();

    String getDefinition();

    T getDefaultValue();

    Function<String, T> parse(String value);
}
```

