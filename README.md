# Vellum

A Java TUI framework for building nested, navigable, live-updating terminal UIs. The
screen is a tree of Sections; the framework owns the run loop, layout, focus, repaint,
resize, and thread marshalling. JLine is used internally behind a swappable terminal
interface and does not appear in the public API.

- Java 8+

## Build

```
./gradlew build      # compile, test, assemble
./gradlew test       # run the test suite (headless)
```

## Run the demo

Install and launch the start script:

```
./gradlew installDist
build/install/Vellum/bin/Vellum        # Vellum.bat on Windows
```

## Libraries

| Library | Version | Use |
|---|---|---|
| [JLine 3](https://github.com/jline/jline3) | 3.25.1 | Terminal driver (raw mode, alternate screen, input); runtime |
| [JUnit 5](https://junit.org/junit5/) | 5.9.1 | Tests |

## Documentation

Full documentation is in [md-docs/index.md](md-docs/index.md). New users should start with the
[step-by-step tutorial](md-docs/tutorial.md).

## License

[MIT](LICENSE)
