# Vellum

A Java TUI framework for building nested, navigable, live-updating terminal UIs. The
screen is a tree of Sections; the framework owns the run loop, layout, focus, repaint,
resize, and thread marshalling. JLine is used internally behind a swappable terminal
interface and does not appear in the public API.

- Java 8+
- Single runtime dependency: JLine 3
- Render model: double-buffered cell diff (only changed cells are written)

## Build

```
./gradlew build      # compile, test, assemble
./gradlew test       # run the test suite (headless)
```

## Run the demo

The demo needs a real terminal:

```
./gradlew run --console=plain
```

If Gradle does not provide a TTY, install and launch the start script:

```
./gradlew installDist
build/install/Vellum/bin/Vellum        # Vellum.bat on Windows
```

Keys: Tab switches pane, Up/Down navigate, Left/Right switch tabs, q quits.

## Minimal program

```java
import com.tonic.vellum.App;
import com.tonic.vellum.Section;
import com.tonic.vellum.layout.Constraint;
import com.tonic.vellum.layout.Slot;
import com.tonic.vellum.layout.Split;
import com.tonic.vellum.widget.LabelSection;
import com.tonic.vellum.widget.MenuSection;

public class Hello {
    public static void main(String[] args) {
        MenuSection menu = new MenuSection("One", "Two", "Three");
        LabelSection body = new LabelSection("Body");

        Section root = Split.horizontal(
                Slot.of(Constraint.fixed(16), menu.bordered("Menu")),
                Slot.of(Constraint.fill(), body.bordered()));

        App.builder()
                .root(root)
                .focusOrder(menu)
                .onQuitKey('q')
                .build()
                .run();
    }
}
```

## Libraries

| Library | Version | Use |
|---|---|---|
| [JLine 3](https://github.com/jline/jline3) | 3.25.1 | Terminal driver (raw mode, alternate screen, input); runtime |
| [JUnit 5](https://junit.org/junit5/) | 5.9.1 | Tests |

## Documentation

Full documentation is in [docs/index.md](docs/index.md). New users should start with the
[step-by-step tutorial](docs/tutorial.md).

## License

[MIT](LICENSE)
