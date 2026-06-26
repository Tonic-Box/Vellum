# Tutorial: Building the Dashboard

[Back to index](index.md)

This builds the demo program (`com.tonic.vellum.examples.DashboardDemo`) one step at a
time: a left menu driving a right-hand tabbed view, with a header and status bar, a
live-updating log, and a metrics panel on a timer.

Each step shows the **whole class** so you always have the full picture; `// NEW` and
`// CHANGED` comments mark what moved. Each step compiles and runs. Run in a real terminal
(see [Getting Started](getting-started.md)) and press `q` to quit.

---

## Step 1: a Section on the screen

Everything visible is a [Section](sections.md). Start with one built-in widget wrapped in a
border, driven by an [App](app.md).

```java
import com.tonic.vellum.App;
import com.tonic.vellum.widget.LabelSection;

public class Dashboard {
    public static void main(String[] args) {
        LabelSection header = new LabelSection("Vellum Dashboard");

        App.builder()
                .root(header.bordered())   // the top of the Section tree, wrapped in a border
                .focusOrder(header)        // which sections can hold focus
                .onQuitKey('q')            // quit when 'q' is pressed
                .build()
                .run();                    // installs the terminal, runs the loop, blocks until quit
    }
}
```

- `root(...)` is the top of the Section tree. `header.bordered()` wraps the label in a
  [BorderSection](widgets.md).
- `run()` installs raw mode and the alternate screen, runs the event loop, and restores the
  terminal on exit (even on an exception).

---

## Step 2: layout with Split

A [Split](layout.md) divides its area among children by [Constraint](layout.md)s, and is
itself a Section, so layouts nest. Stack a header, a body, and a status bar.

```java
import com.tonic.vellum.App;
import com.tonic.vellum.Section;                 // NEW
import com.tonic.vellum.layout.Constraint;       // NEW
import com.tonic.vellum.layout.Slot;             // NEW
import com.tonic.vellum.layout.Split;            // NEW
import com.tonic.vellum.widget.Alignment;        // NEW
import com.tonic.vellum.widget.LabelSection;
import com.tonic.vellum.widget.StatusBar;        // NEW

public class Dashboard {
    public static void main(String[] args) {
        LabelSection header = new LabelSection("Vellum Dashboard")
                .alignment(Alignment.CENTER);     // NEW: center the title
        LabelSection body = new LabelSection("Body"); // NEW: placeholder for now

        // NEW: stack header / body / status bar vertically.
        // fixed(3) = three rows, fill() = the rest, fixed(1) = one row. They tile exactly.
        Section ui = Split.vertical(
                Slot.of(Constraint.fixed(3), header.bordered()),
                Slot.of(Constraint.fill(), body.bordered()),
                Slot.of(Constraint.fixed(1), new StatusBar("q: quit")));

        App.builder()
                .root(ui)                          // CHANGED: root is now the split
                .focusOrder(header)
                .onQuitKey('q')
                .build()
                .run();
    }
}
```

`Slot.of(constraint, section)` pairs a size rule with a child. Resizing the terminal
reflows everything automatically.

---

## Step 3: a menu driving tabs

Put a [MenuSection](widgets.md) on the left and a [TabHost](widgets.md) on the right; the
menu's `onSelect` switches the active tab. (The tabs hold placeholder labels for now.)

```java
import com.tonic.vellum.App;
import com.tonic.vellum.Section;
import com.tonic.vellum.focus.Navigation;         // NEW
import com.tonic.vellum.layout.Constraint;
import com.tonic.vellum.layout.Slot;
import com.tonic.vellum.layout.Split;
import com.tonic.vellum.widget.Alignment;
import com.tonic.vellum.widget.LabelSection;
import com.tonic.vellum.widget.MenuSection;        // NEW
import com.tonic.vellum.widget.StatusBar;
import com.tonic.vellum.widget.TabHost;            // NEW

public class Dashboard {
    public static void main(String[] args) {
        LabelSection header = new LabelSection("Vellum Dashboard")
                .alignment(Alignment.CENTER);

        // NEW: three tabs; only the active one renders. Placeholder content for now.
        TabHost detail = new TabHost()
                .add("Logs", new LabelSection("logs..."))
                .add("Metrics", new LabelSection("metrics..."))
                .add("Config", new LabelSection("config..."));

        // NEW: a menu; Enter on a row switches the matching tab.
        MenuSection menu = new MenuSection("Logs", "Metrics", "Config")
                .onSelect(detail::select);

        // NEW: the body is now menu (fixed width) | detail (fills the rest).
        Section body = Split.horizontal(
                Slot.of(Constraint.fixed(20), menu.bordered("Menu")),
                Slot.of(Constraint.fill(), detail.bordered()));

        Section ui = Split.vertical(
                Slot.of(Constraint.fixed(3), header.bordered()),
                Slot.of(Constraint.fill(), body),  // CHANGED: the horizontal split
                Slot.of(Constraint.fixed(1),
                        new StatusBar("TAB: switch pane   up/down: navigate   left/right: tabs   q: quit")));

        App.builder()
                .root(ui)
                .focusOrder(menu, detail)          // CHANGED: two focus targets; TAB cycles them
                .navigation(Navigation.spatial())  // NEW: arrows move focus by adjacency
                .onQuitKey('q')
                .build()
                .run();
    }
}
```

- Tab cycles the two panes; the parked pane's highlight dims (a Section reads `isFocused()`
  when rendering). With the menu focused, Up/Down move the selection and Enter fires
  `onSelect`. With the detail focused, Left/Right switch tabs. See
  [Focus and Input](focus-and-input.md).
- Switching tabs fires `onUnmount`/`onMount` on the children, so a hidden tab can pause work.

---

## Step 4: content widgets and a custom Section

Replace the placeholders with a scrolling [LogSection](widgets.md), a static
[TextSection](widgets.md), and a custom metrics panel. A custom Section overrides `render`
to draw and calls `requestRedraw()` when its state changes.

```java
import com.tonic.vellum.App;
import com.tonic.vellum.Canvas;                    // NEW
import com.tonic.vellum.Section;
import com.tonic.vellum.focus.Navigation;
import com.tonic.vellum.layout.Constraint;
import com.tonic.vellum.layout.Slot;
import com.tonic.vellum.layout.Split;
import com.tonic.vellum.style.Color;               // NEW
import com.tonic.vellum.style.Style;               // NEW
import com.tonic.vellum.widget.Alignment;
import com.tonic.vellum.widget.LabelSection;
import com.tonic.vellum.widget.LogSection;         // NEW
import com.tonic.vellum.widget.MenuSection;
import com.tonic.vellum.widget.StatusBar;
import com.tonic.vellum.widget.TabHost;
import com.tonic.vellum.widget.TextSection;        // NEW

public class Dashboard {

    // NEW: a custom Section. render() draws in local coordinates onto a clipped Canvas;
    // tick() changes state and asks for a repaint.
    static final class MetricsSection extends Section {
        private int seconds;

        void tick() {
            seconds++;
            requestRedraw();   // mark dirty; the loop repaints only the changed cells
        }

        @Override
        protected void render(Canvas canvas) {
            canvas.put(1, 1, "Uptime: " + seconds + "s");
            canvas.put(1, 3, "Status: OK", Style.NORMAL.fg(Color.GREEN).bold(true));
        }
    }

    public static void main(String[] args) {
        LabelSection header = new LabelSection("Vellum Dashboard")
                .alignment(Alignment.CENTER);

        // NEW: real tab contents
        LogSection logs = new LogSection();
        MetricsSection metrics = new MetricsSection();
        TextSection config = new TextSection("host = localhost\nport = 8080");

        TabHost detail = new TabHost()
                .add("Logs", logs)             // CHANGED: real widgets instead of placeholders
                .add("Metrics", metrics)
                .add("Config", config);

        MenuSection menu = new MenuSection("Logs", "Metrics", "Config")
                .onSelect(detail::select);

        Section body = Split.horizontal(
                Slot.of(Constraint.fixed(20), menu.bordered("Menu")),
                Slot.of(Constraint.fill(), detail.bordered()));

        Section ui = Split.vertical(
                Slot.of(Constraint.fixed(3), header.bordered()),
                Slot.of(Constraint.fill(), body),
                Slot.of(Constraint.fixed(1),
                        new StatusBar("TAB: switch pane   up/down: navigate   left/right: tabs   q: quit")));

        App.builder()
                .root(ui)
                .focusOrder(menu, detail)
                .navigation(Navigation.spatial())
                .onQuitKey('q')
                .build()
                .run();
    }
}
```

The [Canvas](rendering.md) is clipped to the Section's bounds, so out-of-bounds writes are
discarded. `requestRedraw()` must be called on the UI thread.

---

## Step 5: live updates from other threads

All Section methods run on one UI thread. Background work reaches the UI through `App.post`,
and timers through `App.scheduleAtFixedRate`. To use them, keep a reference to the built
`App`.

```java
import com.tonic.vellum.App;
import com.tonic.vellum.Canvas;
import com.tonic.vellum.Section;
import com.tonic.vellum.focus.Navigation;
import com.tonic.vellum.layout.Constraint;
import com.tonic.vellum.layout.Slot;
import com.tonic.vellum.layout.Split;
import com.tonic.vellum.style.Color;
import com.tonic.vellum.style.Style;
import com.tonic.vellum.widget.Alignment;
import com.tonic.vellum.widget.LabelSection;
import com.tonic.vellum.widget.LogSection;
import com.tonic.vellum.widget.MenuSection;
import com.tonic.vellum.widget.StatusBar;
import com.tonic.vellum.widget.TabHost;
import com.tonic.vellum.widget.TextSection;

import java.time.Duration;                                   // NEW
import java.util.concurrent.atomic.AtomicBoolean;            // NEW

public class Dashboard {

    static final class MetricsSection extends Section {
        private int seconds;

        void tick() {
            seconds++;
            requestRedraw();
        }

        @Override
        protected void render(Canvas canvas) {
            canvas.put(1, 1, "Uptime: " + seconds + "s");
            canvas.put(1, 3, "Status: OK", Style.NORMAL.fg(Color.GREEN).bold(true));
        }
    }

    public static void main(String[] args) {
        LabelSection header = new LabelSection("Vellum Dashboard")
                .alignment(Alignment.CENTER);

        LogSection logs = new LogSection();
        MetricsSection metrics = new MetricsSection();
        TextSection config = new TextSection("host = localhost\nport = 8080");

        TabHost detail = new TabHost()
                .add("Logs", logs)
                .add("Metrics", metrics)
                .add("Config", config);

        MenuSection menu = new MenuSection("Logs", "Metrics", "Config")
                .onSelect(detail::select);

        Section body = Split.horizontal(
                Slot.of(Constraint.fixed(20), menu.bordered("Menu")),
                Slot.of(Constraint.fill(), detail.bordered()));

        Section ui = Split.vertical(
                Slot.of(Constraint.fixed(3), header.bordered()),
                Slot.of(Constraint.fill(), body),
                Slot.of(Constraint.fixed(1),
                        new StatusBar("TAB: switch pane   up/down: navigate   left/right: tabs   q: quit")));

        App app = App.builder()        // CHANGED: keep the App so we can post/schedule
                .root(ui)
                .focusOrder(menu, detail)
                .navigation(Navigation.spatial())
                .onQuitKey('q')
                .build();

        // NEW: tick the metrics panel once a second, on the UI thread
        app.scheduleAtFixedRate(Duration.ofSeconds(1), Duration.ofSeconds(1), metrics::tick);

        // NEW: a background thread streams log lines onto the UI thread via post
        AtomicBoolean running = new AtomicBoolean(true);
        Thread feeder = new Thread(() -> {
            int n = 0;
            while (running.get()) {
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                final int id = n++;
                app.post(() -> logs.append("processed request " + id)); // runs on the UI thread
            }
        }, "log-feeder");
        feeder.setDaemon(true);
        feeder.start();

        try {
            app.run();
        } finally {                    // NEW: stop the feeder when the app exits
            running.set(false);
            feeder.interrupt();
        }
    }
}
```

- `post(Runnable)` is the only thread-safe entry point; the runnable runs on the UI thread,
  where `logs.append(...)` (which calls `requestRedraw()`) is legal.
- `scheduleAtFixedRate` runs its task on the UI thread too (it marshals via `post`).
- Calling `requestRedraw()` directly from a background thread throws; always go through
  `post`.

This is the finished program. The shipped version, with a richer log feeder, is
`com.tonic.vellum.examples.DashboardDemo`. Run it from a real terminal:

```
./gradlew run --console=plain
```

## Next

- [Core Concepts](core-concepts.md) for the run loop, dirty tracking, and threading.
- [Widgets](widgets.md) for every built-in Section, including `TextInput` and overlays.
- [Sections](sections.md) for writing your own containers.
