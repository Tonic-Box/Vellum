# Focus and Input

[Back to index](index.md)

Focus is framework-owned. You declare the participants and order; the framework routes keys
and fires focus hooks. You never hand-route Tab.

## The focus ring

Declare an ordered ring on the builder:

```java
App.builder().root(ui).focusOrder(menu, detail).build();
```

- Tab advances to the next target; Shift-Tab goes back; both wrap.
- On a focus change the framework calls `onFocusLost` on the old target and `onFocusGained`
  on the new, and repaints both so each can update its highlight or border.

## Key dispatch

For each key:

1. Deliver to the deepest node on the focus path (the focused target, or the active nested
   target inside a `FocusContainer`).
2. If `UNHANDLED`, bubble up the parent chain; each ancestor gets `onKey`.
3. If still `UNHANDLED` and the key is Tab or Shift-Tab, the focus manager moves focus.
4. If a `Navigation` strategy is set and the key is an unhandled arrow, it may move focus.
5. Otherwise the key is dropped.

A Section consumes what it owns and bubbles the rest, so no central key table is needed.
Arrow keys belong to the focused Section unless it leaves them unhandled.

## isFocused

`Section.isFocused()` is true for every Section on the focus path: the focused target plus
any active nested targets. A `BorderSection` checks its child's `isFocused()` to draw a
focused frame.

## FocusContainer

`com.tonic.vellum.focus.FocusContainer` lets a container expose internal focus stops, so
the focus path can descend into it. [TabHost](widgets.md) implements it so its active
content is on the focus path and receives keys first.

| Method | Description |
|---|---|
| `List<Section> focusTargets()` | Ordered internal focusable descendants. |
| `Section activeFocusTarget()` | The current internal target, or null. |
| `boolean advanceFocus(boolean forward)` | Move internal focus; return false at an edge so the parent advances. |

A container's `advanceFocus` decides its Tab behavior: `TabHost` returns false so Tab crosses
panes, while [Form](widgets.md) cycles Tab among its own fields. Field-level
`onFocusGained`/`onFocusLost` fire as internal focus moves. A container that changes its
active target on its own (e.g. `TabHost.select`) calls the protected `Section.refreshFocus()`
so the focus path, key routing, and cursor follow the new target.

## Forms

[Form](widgets.md) implements `FocusContainer` to give in-container Tab traversal: add fields
(`Button`, `Checkbox`, `TextInput`, etc.), and Tab/Shift-Tab cycle them; nested forms (such as
a horizontal button row) are traversed depth-first. This backs the dialog helpers and any
multi-field UI.

## Modal overlays

Focus is a stack of scopes; the base UI is the bottom scope. Opening a modal overlay (see
[App](app.md)) pushes a scope, so the overlay's targets receive all keys and the base UI is
parked until the overlay closes, which pops the scope and restores focus beneath.

## Navigation

`com.tonic.vellum.focus.Navigation` moves focus by arrow keys when the focused Section
leaves an arrow unhandled.

```java
App.builder().navigation(Navigation.spatial()).build();
```

| Method | Description |
|---|---|
| `Section resolve(Section current, Key direction, List<Section> targets)` | The target reached by an arrow, or null. |
| `static Navigation spatial()` | Adjacency by on-screen bounds. |

## KeyEvent

`com.tonic.vellum.input.KeyEvent` is an immutable, normalized key.

| Method | Description |
|---|---|
| `Key code()` | Logical key. |
| `char ch()` | Character, valid when `code() == CHAR`. |
| `boolean ctrl()`, `alt()`, `shift()` | Modifiers. |
| `boolean is(Key)` | True if the logical key matches. |

Construction (mainly for tests): `KeyEvent.special(Key)`, `KeyEvent.special(Key, ctrl, alt,
shift)`, `KeyEvent.character(char)`, `KeyEvent.character(char, ctrl, alt, shift)`.

## Key

`com.tonic.vellum.input.Key` values:

```
UP DOWN LEFT RIGHT
ENTER ESCAPE TAB SHIFT_TAB BACKSPACE DELETE
HOME END PAGE_UP PAGE_DOWN
CHAR CTRL_TAB UNKNOWN
```

`CHAR` carries a printable character in `ch()`. `CTRL_TAB` is recognized only where the
terminal reports it.

## KeyResult

`com.tonic.vellum.KeyResult` is `CONSUMED` (handled, stop propagation) or `UNHANDLED`
(bubble to parent and focus manager).
