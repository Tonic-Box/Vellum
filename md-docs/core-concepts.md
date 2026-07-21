# Core Concepts

[Back to index](index.md)

## The Section tree

The screen is a tree of [Sections](sections.md). Each Section is given a rectangular area
(its bounds) and renders into a [Canvas](rendering.md) clipped to that area, in local
coordinates. Container Sections ([Split](layout.md), [TabHost](widgets.md),
[BorderSection](widgets.md)) own children and assign each a sub-rectangle. Composition is
by nesting Sections.

## The run loop

[App](app.md) owns a single UI thread. One loop iteration:

1. Drain the task queue (runnables from `post` and due scheduled tasks).
2. Poll input for up to ~16 ms, then dispatch all keys buffered this frame.
3. Apply a pending resize: reallocate buffers, re-solve layout, force a full repaint.
4. If anything is dirty, render dirty Sections into the back buffer, diff against the
   front buffer, write only the changed cells, and reposition the cursor.

An idle UI (nothing dirty, no input) writes nothing and uses negligible CPU.

Exceptions thrown by a task, key handler, or render are isolated: the loop routes them to
the `onError` handler (see [App](app.md)) and keeps running. The terminal is always
restored on exit.

## Dirty tracking and repaint

`requestRedraw()` marks a Section dirty (content changed). The next loop iteration
re-renders dirty Sections and the cell diff emits only changed cells. Layout is not
re-solved for a content redraw; it is re-solved only when bounds change (resize or a
structural change such as a tab switch), which cascades through `onResize`.

## Threading

All Section methods run on the UI thread. Background threads must marshal work onto it:

```java
App app = App.current();
backgroundWork(line -> app.post(() -> {
    logSection.append(line);   // runs on the UI thread
}));
```

`post(Runnable)` is the only thread-safe entry point. `requestRedraw()` is UI-thread-only;
calling it off-thread throws
`IllegalStateException`. See [App and Run Loop](app.md) for `post`, `schedule`, and
`scheduleAtFixedRate`.

## Focus

Focus is framework-owned. You declare an ordered ring of focus targets on the builder;
Tab and Shift-Tab cycle them. Keys are delivered to the focused target first, then bubble
up its parent chain, then to the focus manager. Arrow keys are owned by the focused
Section unless it leaves them unhandled and spatial navigation is enabled. See
[Focus and Input](focus-and-input.md).

## Rendering pipeline

The Canvas writes styled cells into an in-memory back buffer. On repaint the framework
diffs the back buffer against the front buffer (the last emitted frame) and writes only
the changed cells, minimizing cursor moves and style changes. Sections never emit escape
codes. See [Rendering](rendering.md).
