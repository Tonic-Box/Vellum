# App and Run Loop

[Back to index](index.md)

`com.tonic.vellum.App` owns the terminal, the UI thread, the event loop, layout, repaint,
focus, and resize.

## Building an app

```java
App app = App.builder()
        .root(rootSection)
        .focusOrder(menu, detail)
        .navigation(Navigation.spatial())   // optional
        .initialFocus(menu)                  // optional; default is first in order
        .onQuitKey('q')                      // optional
        .build();
app.run();
```

### Builder methods

| Method | Description |
|---|---|
| `root(Section)` | Root of the Section tree. Required. |
| `focusOrder(Section...)` | Ordered ring cycled by Tab / Shift-Tab. |
| `navigation(Navigation)` | Spatial arrow navigation. See [Focus and Input](focus-and-input.md). |
| `initialFocus(Section)` | Target focused at startup. Default: first in the ring. |
| `onQuit(Predicate<KeyEvent>)` | Quit when the predicate matches a key. |
| `onQuitKey(Key)` | Quit on a logical key. |
| `onQuitKey(char)` | Quit on a printable character. |
| `onError(Consumer<Throwable>)` | Handle exceptions from tasks, key handlers, and renders. |
| `build()` | Build the `App`. Throws if no root is set. |

The quit key is checked before dispatch, so it works even if a section misbehaves. With a
focused `TextInput`, use a non-printable quit (Ctrl-C or Escape) so the quit key is not
typed into the field.

## App methods

| Method | Description |
|---|---|
| `static App builder()` | New builder. |
| `static App current()` | The running instance, or null. |
| `void run()` | Install the terminal and run the loop until `quit()`. Blocks. On exit, unmounts the tree (firing `onUnmount`) and restores the terminal. |
| `void quit()` | Stop the loop; `run()` then unmounts and restores. |
| `void post(Runnable)` | Marshal a task onto the UI thread. Thread-safe. |
| `Cancellable schedule(Duration delay, Runnable)` | Run a task once on the UI thread after a delay. |
| `Cancellable scheduleAtFixedRate(Duration initial, Duration period, Runnable)` | Run a repeating task on the UI thread. |
| `OverlayHandle openOverlay(Section content, Placement, Section... focusTargets)` | Open a modal overlay. UI-thread-only. |

## Threading

All Section methods run on the UI thread. Background threads use `post`:

```java
new Thread(() -> {
    for (String line : stream) {
        app.post(() -> logs.append(line));
    }
}).start();
```

`post` is safe from any thread. `schedule` and `scheduleAtFixedRate` run their task on the
UI thread (the timer fires on an internal thread, then marshals via `post`).

`requestRedraw()` and all lifecycle and input methods are UI-thread-only and guarded;
calling them off-thread throws `IllegalStateException`.

## Scheduling and Cancellable

```java
Cancellable ticker = app.scheduleAtFixedRate(
        Duration.ofSeconds(1), Duration.ofSeconds(1), metrics::tick);
// later
ticker.cancel();
```

`com.tonic.vellum.Cancellable`:

| Method | Description |
|---|---|
| `void cancel()` | Cancel the scheduled task. Idempotent. |
| `boolean isCancelled()` | True once cancelled. |

## Error handling

If a task, key handler, or section render throws, the loop keeps running and the exception
goes to the `onError` handler (called on the UI thread). Without a handler, the first error
is printed after the terminal is restored. The terminal is always restored on exit - on a
clean quit, on an unhandled exception, and on a process kill (via a JVM shutdown hook).

## Overlays

`openOverlay` renders a section on top of the UI and routes keys to it modally. It pushes a
focus scope, so Tab cycles the overlay's `focusTargets` (or the content itself if none are
given) and the base UI is parked until the overlay closes. The overlay content closes
itself through the returned `OverlayHandle`. Close overlays in reverse order of opening.

```java
TextInput field = new TextInput().placeholder("name");
OverlayHandle dialog = app.openOverlay(
        field.bordered("Rename"), Placement.centered(30, 3));
field.onSubmit(name -> { apply(name); dialog.close(); });
```

`Placement` computes the overlay bounds from the screen:

| Method | Description |
|---|---|
| `static Placement centered(int width, int height)` | Centered, clamped to the screen. |
| `static Placement fixed(Rect)` | A fixed rectangle. |

`OverlayHandle`:

| Method | Description |
|---|---|
| `void close()` | Remove the overlay and restore focus. UI-thread-only. Idempotent. |
| `boolean isOpen()` | True until closed. |

## Run loop reference

One iteration: drain task queue, poll and dispatch all buffered input, apply pending
resize, repaint dirty Sections, reposition the cursor. See [Core Concepts](core-concepts.md)
for detail.
