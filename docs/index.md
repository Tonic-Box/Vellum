# Vellum Documentation

Vellum builds terminal UIs as a tree of Sections. A Section renders into a clipped Canvas,
handles keys when focused, and calls `requestRedraw()` to mark itself dirty. Container
Sections own children and assign them sub-rectangles. The framework owns the run loop,
layout, focus, dirty-tracking repaint, resize, and thread marshalling.

## Guides

- [Getting Started](getting-started.md) - install, build, run, first program.
- [Tutorial](tutorial.md) - build the dashboard demo step by step.
- [Core Concepts](core-concepts.md) - the mental model, run loop, threading, repaint.

## API Reference

- [App and Run Loop](app.md) - `App`, `App.Builder`, scheduling, `Cancellable`.
- [Sections](sections.md) - `Section` base class, lifecycle, custom sections, containers.
- [Layout](layout.md) - `Split`, `Constraint`, `Slot`, `Axis`, `LayoutSolver`.
- [Widgets](widgets.md) - built-in Sections.
- [Focus and Input](focus-and-input.md) - focus ring, `FocusContainer`, `Navigation`, keys.
- [Rendering](rendering.md) - `Canvas`, `Rect`, `Style`, `Color`.
- [Terminal](terminal.md) - `Terminal`, `Terminals`, `TerminalSize`.

## Packages

| Package | Contents |
|---|---|
| `com.tonic.vellum` | `Section`, `App`, `Canvas`, `KeyResult`, `Cancellable`, `Placement`, `OverlayHandle`, `CharWidth`, `Maths` |
| `com.tonic.vellum.geom` | `Rect`, `Point` |
| `com.tonic.vellum.style` | `Style`, `Color` |
| `com.tonic.vellum.input` | `Key`, `KeyEvent` |
| `com.tonic.vellum.layout` | `Split`, `Constraint`, `Slot`, `Axis`, `LayoutSolver` |
| `com.tonic.vellum.focus` | `FocusContainer`, `Navigation` |
| `com.tonic.vellum.terminal` | `Terminal`, `Terminals`, `TerminalSize` |
| `com.tonic.vellum.widget` | Text: `LabelSection`, `TextSection`, `StatusBar`, `TextInput`. Lists: `AbstractListSection`, `MenuSection`, `SelectList`, `Table`, `RadioGroup`. Containers: `BorderSection`, `TabHost`, `Form`. Controls: `Button`, `Checkbox`. Dialogs: `Dialogs`. Indicators: `ProgressBar`, `Spinner`, `Sparkline`. Tree: `TreeView`, `TreeNode`. Scroll: `ScrollSection`, `LogSection`. Base: `SingleRowSection`. `Alignment` |
