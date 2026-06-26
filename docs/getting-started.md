# Getting Started

[Back to index](index.md)

## Requirements

- Java 8 or later
- Gradle (the wrapper is included)

## Build and test

```
./gradlew build      # compile, test, assemble
./gradlew test       # run the test suite
```

## First program

A two-pane layout: a bordered menu on the left, a bordered label on the right. Tab moves
focus between panes; q quits.

```java
import com.tonic.vellum.App;
import com.tonic.vellum.Section;
import com.tonic.vellum.layout.Constraint;
import com.tonic.vellum.layout.Slot;
import com.tonic.vellum.layout.Split;
import com.tonic.vellum.widget.Alignment;
import com.tonic.vellum.widget.LabelSection;
import com.tonic.vellum.widget.MenuSection;

public class Hello {
    public static void main(String[] args) {
        LabelSection body = new LabelSection("Body").alignment(Alignment.CENTER);
        MenuSection menu = new MenuSection("One", "Two", "Three")
                .onSelect(index -> body.setText("Selected " + index));

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

`run()` blocks until `quit()` (here triggered by q). It installs raw mode and the
alternate screen on entry and restores the terminal on exit, including on exceptions.

## Run it

A TUI needs a real terminal. From the project that depends on Vellum:

```
./gradlew run --console=plain
```

Or build a start script and run it directly:

```
./gradlew installDist
build/install/<name>/bin/<name>
```

## Next

- [Tutorial](tutorial.md) builds a full dashboard step by step.
- [Core Concepts](core-concepts.md) for the run loop and threading model.
- [Widgets](widgets.md) for the built-in Sections.
