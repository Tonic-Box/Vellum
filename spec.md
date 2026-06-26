# Java TUI Framework — Design & API Specification

## 0. Purpose & scope

This document specifies the **public API contract and architectural model** for a terminal UI (TUI) framework library for Java. It is an implementation brief: it defines the mental model, the core types, their contracts, and the runtime behavior the implementation must satisfy. It deliberately leaves internal data structures and terminal-driver details to the implementer except where they are load-bearing for the contract.

**Design goal:** an intuitive, programmatic API for building complex TUIs — nested layouts, hot-swappable tabbed subviews, arrow-key navigation, per-section live updates from background threads — without exposing the lifecycle/threading complexity that makes lower-level libraries (e.g. JLine) hard to use directly. The framework may use JLine (or any equivalent) internally as a terminal driver, but that must not leak into the public contract.

**Non-goals (for v1):** mouse support, true-color theming systems, complex text reflow/word-wrap engines, accessibility integrations. Design types so these can be added later without breaking changes, but do not implement them now.

---

## 1. The mental model (one paragraph)

The screen is a tree of **Sections**. A Section renders itself into a pre-clipped rectangular **Canvas** it is given, handles key events only when it has focus, and calls `requestRedraw()` to mark itself dirty. Container Sections (splits, tab hosts) own child Sections and assign them sub-rectangles. The framework owns the run loop, layout solving, focus traversal, dirty-tracking/repaint, resize handling, and thread-safe marshalling of external updates onto the UI thread. Everything the user builds is a Section; composition is by nesting Sections.

This sentence is the contract. If a user holds this model, they can build a tabbed, navigable, live-updating dashboard without reading further.

---

## 2. Core abstractions

### 2.1 `Section` (abstract base)

The single universal unit. Every visible thing is a Section. Containers are Sections that hold Sections.

```java
public abstract class Section {

    // ---- framework-assigned state (not user-set) ----
    private Rect bounds;            // assigned by parent during layout pass
    private Section parent;         // null for root
    private boolean dirty = true;

    // ---- rendering ----
    /**
     * Draw this section. The canvas is ALREADY clipped to this section's bounds;
     * coordinates passed to canvas methods are LOCAL (0,0 = top-left of this section).
     * Implementations MUST NOT assume they can draw outside the canvas; out-of-bounds
     * writes are silently discarded by the Canvas.
     */
    protected abstract void render(Canvas canvas);

    // ---- input ----
    /**
     * Handle a key event. Return CONSUMED if handled (stops propagation),
     * UNHANDLED to let it bubble to the parent / focus manager.
     * Called only when this section (or a descendant that returned UNHANDLED)
     * is on the focus path. Default: UNHANDLED.
     */
    protected KeyResult onKey(KeyEvent key) { return KeyResult.UNHANDLED; }

    // ---- lifecycle hooks (all optional, default no-op) ----
    protected void onMount() {}          // became part of the live tree / shown
    protected void onUnmount() {}        // removed from live tree / hidden
    protected void onFocusGained() {}    // this section received focus
    protected void onFocusLost() {}      // this section lost focus
    protected void onResize(Rect newBounds) {} // bounds changed (incl. terminal resize)

    // ---- user-callable API ----
    /** Mark this section dirty; framework batches and repaints the dirty subtree. */
    public final void requestRedraw() { /* sets dirty, notifies App */ }

    /** True if this section currently holds input focus. */
    public final boolean isFocused() { /* ... */ }

    public final Rect bounds() { return bounds; }
    public final Section parent() { return parent; }
}
```

**Contract requirements:**

- `render` receives a Canvas clipped to `bounds`. The Canvas MUST reject (silently no-op) any write outside its rectangle. This clipping guarantee is central and non-negotiable.
- `onKey` return value drives propagation. `UNHANDLED` MUST bubble to the parent chain and ultimately to the focus manager (see §5).
- Lifecycle hooks MUST fire on the **UI thread** only.
- `requestRedraw()` MUST be callable from any thread? — **No.** `requestRedraw()` is UI-thread-only. Cross-thread updates go through `App.post(...)` (see §6) which marshals onto the UI thread and may itself call `requestRedraw()`. The implementation should assert/guard against off-thread `requestRedraw()` in debug builds.

### 2.2 `Canvas`

A drawing surface clipped to a Section's bounds. Backed by the framework's double buffer (see §7); the user never sees raw escape codes.

```java
public interface Canvas {
    int width();
    int height();
    Rect bounds();                 // local bounds: always origin (0,0), size w×h

    // primitive cell writes (local coordinates)
    void put(int x, int y, char c);
    void put(int x, int y, char c, Style style);
    void put(int x, int y, String text);              // single line, truncated at width
    void put(int x, int y, String text, Style style);

    void fill(Rect area, char c, Style style);
    void clear();                  // fill bounds with spaces / default style

    // sub-canvas for manual child layout (returns a further-clipped Canvas)
    Canvas clip(Rect sub);

    // convenience for carving regions (see Rect helpers)
    // e.g. canvas.bounds().takeTop(1) -> Rect for a one-row strip
}
```

**Contract:** all coordinates are local to the canvas. Writes outside `[0,width) × [0,height)` are discarded. `clip(sub)` returns a Canvas whose origin is the sub-rect's top-left, recursively clipped.

### 2.3 `Rect`

Immutable rectangle with ergonomic carving helpers (used by both layout and tab-bar/border logic).

```java
public record Rect(int x, int y, int width, int height) {
    int right();   // x + width
    int bottom();  // y + height

    Rect takeTop(int rows);        // top strip of given height
    Rect takeBottom(int rows);
    Rect takeLeft(int cols);
    Rect takeRight(int cols);
    Rect remainder();              // what's left after the most recent take* (see note)
    Rect inset(int amount);        // shrink on all sides (for borders)
    boolean contains(int px, int py);
}
```

> Note on `remainder()`: prefer a non-stateful design. Instead of a mutating cursor, provide pure splits: `Rect[] splitTop(int rows)` returning `{top, rest}`. The implementer should choose pure helpers over stateful `take/remainder` to avoid surprising aliasing. The spec mandates **pure, non-mutating** Rect helpers.

### 2.4 `Style`

Text styling value object. Keep minimal for v1.

```java
public final class Style {
    public static final Style NORMAL;
    public static final Style REVERSE;       // inverted fg/bg
    public static final Style DIM_REVERSE;   // for "parked" selections
    public static final Style BOLD;
    public static final Style DIM;

    Style fg(Color c);
    Style bg(Color c);
    Style bold(boolean b);
    Style reverse(boolean b);
    // Style is immutable; setters return new instances.
}
```

`Color` for v1: the 16 ANSI named colors plus `DEFAULT`. Leave room for 256/truecolor later but do not implement.

### 2.5 `KeyEvent` / `Key` / `KeyResult`

```java
public enum KeyResult { CONSUMED, UNHANDLED }

public final class KeyEvent {
    Key code();            // logical key (UP, DOWN, ENTER, TAB, CHAR, ...)
    char ch();             // valid when code() == CHAR
    boolean ctrl();
    boolean alt();
    boolean shift();
    boolean is(Key k);     // convenience, ignores modifiers unless k encodes them
}

public enum Key {
    UP, DOWN, LEFT, RIGHT,
    ENTER, ESCAPE, TAB, BACKSPACE, DELETE,
    HOME, END, PAGE_UP, PAGE_DOWN,
    CHAR,                  // printable char in ch()
    CTRL_TAB,              // common composite; framework normalizes
    // F1..F12 optional for v1
}
```

The terminal driver MUST normalize raw escape sequences into `KeyEvent`s. Composite keys like `CTRL_TAB` should be recognized where the terminal reports them; where a terminal cannot distinguish them, document the limitation rather than faking it.

---

## 3. Layout — `Split` and `Constraint`

Layout is itself a Section (`Split`), keeping one mental model. A `Split` divides its bounds among children by constraints.

```java
public final class Split extends Section {

    public static Split horizontal(Object... constraintAndChildPairs);
    public static Split vertical(Object... constraintAndChildPairs);
    // varargs alternate Constraint, Section, Constraint, Section, ...
    // (Provide a typesafe builder too; see below.)

    public Split add(Constraint size, Section child);  // fluent form
}
```

Two construction forms required:

**Varargs (static layouts):**
```java
Split.vertical(
    Constraint.fixed(3),  header,
    Constraint.fill(),    body,
    Constraint.fixed(1),  statusBar
);
```
> Implementer: the `Object...` varargs form is convenient but untyped. Provide it, but ALSO provide a typesafe builder (`Split.vertical().add(c, s).add(c, s).build()`) and recommend it in docs. If you can express the paired form type-safely (e.g. a `Slot` record `Split.vertical(slot(fixed(3), header), slot(fill(), body))`), prefer that and drop raw `Object...`.

**Fluent (conditional layouts):**
```java
Split split = Split.horizontal()
    .add(Constraint.fixed(20), menu)
    .add(Constraint.fill(), detail);
```

### 3.1 `Constraint`

```java
public abstract class Constraint {
    public static Constraint fixed(int cells);   // exact rows (vertical) / cols (horizontal)
    public static Constraint percent(int pct);   // % of available space
    public static Constraint fill();             // share remaining space equally
    public static Constraint fill(int weight);   // weighted share of remaining
    public static Constraint min(int cells);     // at least N, grow if space
    public static Constraint max(int cells);
}
```

### 3.2 Layout solving contract

A `Layout.solve(Rect available, List<Constraint>)` returns `Rect[]` aligned to the children. Algorithm requirements:

1. Satisfy all `fixed` first.
2. Satisfy `min`/`max` bounds.
3. Distribute remaining space across `percent` (of the ORIGINAL available extent) and `fill` (weighted) constraints.
4. Resolve rounding so the children exactly tile the available rect with no gaps or overlap (assign rounding remainder to the last `fill`, or distribute deterministically).
5. Never produce negative or overlapping rects; clamp to zero-size if space is exhausted (a zero-size section simply renders nothing).

The split direction (horizontal/vertical) determines whether constraints apply to width or height; the cross-axis always gets the full extent.

---

## 4. Tabbed / hot-swappable subviews — `TabHost`

A Section holding N child Sections, rendering exactly one ("active"), swapping instantly. Swapping fires visibility lifecycle on the children.

```java
public final class TabHost extends Section {

    public TabHost add(String title, Section content);

    public void select(int index);
    public void select(String title);
    public int active();
    public int count();

    public TabHost showTabBar(boolean show);     // default true
    public TabHost preserveState(boolean keep);  // default true — see contract
}
```

### 4.1 Contract

- Renders a one-row **tab bar** at the top when `showTabBar` (default). The bar lists titles; the active tab is visually distinguished; if focused, distinguished more strongly.
- Renders **only the active child** into the remaining area (a clipped sub-canvas).
- `select(...)`:
    - If target == active, no-op.
    - Else: call `onUnmount()` on the outgoing active child, set new active, call `onMount()` on the incoming child, then `requestRedraw()`.
- **`preserveState` (default true):** inactive children's Section *instances* are retained (their state persists); only `onMount`/`onUnmount` fire as **visibility** signals. The framework MUST NOT destroy/recreate child instances on tab switch. (`onMount`/`onUnmount` therefore mean "shown"/"hidden" here, consistent with their general meaning of "entered/left the live render path.") A child uses these hooks to e.g. pause a poller when hidden and resume when shown.
- Key handling (default scheme — see §5.3 for rationale):
    - Give the active child first refusal (`child.onKey`).
    - If unhandled, `TabHost` handles tab-switching: `LEFT`/`RIGHT` (and `CTRL_TAB`) move between tabs.
    - Otherwise `UNHANDLED` (bubbles, e.g. `TAB` to cross panes).

```java
@Override
protected KeyResult onKey(KeyEvent key) {
    if (active child consumes key) return CONSUMED;
    switch (key.code()) {
        case LEFT:     select(max(0, active - 1));        return CONSUMED;
        case RIGHT:    select(min(count()-1, active + 1)); return CONSUMED;
        case CTRL_TAB: select((active + 1) % count());     return CONSUMED;
        default:       return UNHANDLED;
    }
}
```

---

## 5. Focus & navigation

Focus is **framework-owned**. The user declares participants and ordering; the framework routes keys and fires focus hooks. The user never hand-routes Tab.

### 5.1 v1 model: flat focus ring with optional nested descent

For v1, support a **flat focus order** of top-level focus targets (typically panes), declared on the App:

```java
App.builder()
    .root(ui)
    .focusOrder(menu, detail)      // TAB / SHIFT-TAB cycle these, in order
    .navigation(Navigation.spatial())  // optional: arrows move focus by adjacency
    .build();
```

- `TAB` advances focus to the next target in the ring; `SHIFT_TAB` goes back; wraps around.
- On focus change: framework calls `onFocusLost()` on the old target, `onFocusGained()` on the new, and repaints both (so each can redraw its border/selection highlight).
- Key dispatch order for each keystroke:
    1. Deliver to the focused target's `onKey`.
    2. If `UNHANDLED`, bubble up its parent chain (each ancestor `onKey`).
    3. If still `UNHANDLED` and the key is `TAB`/`SHIFT_TAB` (or matches a spatial nav binding), the focus manager consumes it and moves focus.
    4. Otherwise the key is dropped.

### 5.2 Nested focus (design for it; partial in v1)

Container Sections often need internal focus stops (a TabHost where the tab *bar* is a stop and the active content is a deeper stop; a form with multiple fields). The architecture MUST support evolving into a **focus tree** without breaking the flat API.

Required provision in v1:
- A Section MAY implement an optional capability interface to expose internal focus stops:

```java
public interface FocusContainer {
    /** Ordered focusable descendants this container manages internally. */
    List<Section> focusTargets();
    /** Currently focused internal target, or null. */
    Section activeFocusTarget();
    /** Move internal focus; return false if at an edge (let parent advance). */
    boolean advanceFocus(boolean forward);
}
```

- The focus manager, when advancing, first asks the current target (if a `FocusContainer`) to `advanceFocus`. If it returns `false` (edge reached), the manager advances at the parent level. This yields correct nested Tab traversal without forcing every Section to participate.

For v1 you may ship the flat ring working end-to-end and `FocusContainer` honored by `TabHost` for the tab-bar-vs-content case; full arbitrary-depth form focus can follow. The API above must be in place so it is non-breaking.

### 5.3 Arrow-key semantics (intentional division)

Arrow keys are **owned by the focused Section**, never globally hijacked (unless `Navigation.spatial()` is enabled, and even then only when the focused Section returns `UNHANDLED` for that arrow). This gives the intuitive scheme:

- Menu focused: `UP`/`DOWN` move the menu selection.
- TabHost focused: `LEFT`/`RIGHT` switch tabs; `UP`/`DOWN` go to the active content (which may scroll).
- A scrollable content Section consumes `UP`/`DOWN`/`PAGE_*` for scrolling.

Because each Section consumes what it owns and bubbles the rest, no central key-routing table is needed.

---

## 6. The application & run loop — `App`

`App` owns the terminal, the single UI thread / event loop, layout, repaint, focus, and resize.

```java
public final class App {

    public static Builder builder();

    public static App current();        // the running instance (for post/scheduleRepaint)

    public void run();                  // blocks until quit(); installs/raw-mode terminal,
                                        // runs the event loop, restores terminal on exit.
    public void quit();                 // stop the loop, restore terminal.

    /** Marshal a runnable onto the UI thread. Safe to call from ANY thread. */
    public void post(Runnable task);

    /** Schedule a repeating/delayed task on the UI thread (for timers/animation). */
    public Cancellable schedule(Duration delay, Runnable task);
    public Cancellable scheduleAtFixedRate(Duration initial, Duration period, Runnable task);

    public static final class Builder {
        Builder root(Section root);
        Builder focusOrder(Section... targets);
        Builder navigation(Navigation nav);     // optional
        Builder initialFocus(Section target);   // optional; default first in order
        Builder onQuitKey(Key key);             // optional convenience (e.g. CTRL+C / 'q')
        App build();
    }
}
```

### 6.1 Run loop contract

The loop runs on a single **UI thread**. One iteration:

1. **Drain the task queue** (runnables from `post`, due `schedule`d tasks). Each may mutate Section state and call `requestRedraw()`.
2. **Poll input** (non-blocking or with a small timeout). For each `KeyEvent`, dispatch per §5.1. Quit key (if configured) triggers `quit()`.
3. **Handle resize**: if the terminal size changed, recompute root bounds, propagate `onResize` down the tree, mark all dirty.
4. **Repaint**: if any Section is dirty, run the layout pass for affected subtrees, call `render` into the back buffer for dirty sections, **diff** against the front buffer, and emit only changed cells to the terminal (see §7). Swap buffers.
5. Sleep/yield to cap frame rate (e.g. target ≤ 60fps; only repaint when dirty — an idle UI emits nothing).

**Threading contract:**
- All Section methods (`render`, `onKey`, lifecycle, `requestRedraw`) are invoked on the UI thread only.
- Any other thread MUST use `App.post(...)` to touch the UI. This is the single concurrency primitive users learn (analogous to Swing `invokeLater`). It is the framework's primary ergonomic differentiator: background work (network, file watch, timers) feeds the UI safely with no locks in user code.

Example — live updates from a background stream:
```java
logStream.onLine(line -> App.current().post(() -> {
    logSection.append(line);
    logSection.requestRedraw();
}));
```

---

## 7. Rendering pipeline — double-buffered cell diff

The Canvas writes into an in-memory **back buffer** of styled cells. On repaint, the framework diffs the back buffer against the **front buffer** (last emitted frame) and writes only changed cells to the terminal, with minimal cursor moves.

**Contract / requirements:**
- A cell = `(char, Style)`. Buffers are `width × height` grids sized to the terminal.
- `render` never emits escape codes directly; it only mutates cells via Canvas.
- Per-section dirty tracking determines which subtrees re-render into the back buffer; the cell diff determines what bytes hit the wire. The combination is what makes "update one section" touch only that section's changed cells.
- The renderer must minimize terminal writes: batch contiguous runs, move the cursor only when needed, set style only on change.
- On resize, both buffers are reallocated and a full repaint is forced.
- Hide the hardware cursor during repaint (or park it); restore terminal state fully on `quit()` (raw mode off, alt-screen off, cursor shown).
- Use the **alternate screen buffer** so the app doesn't clobber the user's scrollback; restore on exit.

This double-buffered diff model is mandatory; direct-write rendering is not acceptable because it cannot deliver correct, flicker-free partial updates.

---

## 8. Built-in Sections to ship in v1

Implement these on top of the core (they are also reference implementations of the contract):

1. **`Split`** — layout (§3).
2. **`TabHost`** — hot-swappable tabs (§4).
3. **`MenuSection`** — vertical, arrow-navigable, top-down list with selection, `onSelect(Consumer<Integer>)` callback, focused/parked highlight states.
4. **`TextSection`** / **`LabelSection`** — static or settable text, optional alignment.
5. **`ScrollSection`** (or a scrollable list base) — vertically scrollable content with `UP`/`DOWN`/`PAGE_*`/`HOME`/`END`, optional follow-tail (for logs).
6. **`BorderSection`** — wraps a single child, draws a border + optional title, insets the child's bounds. (See §9 on chrome ownership.)
7. **`StatusBar`** — single-row text section, convenience.

### 8.1 Reference: `MenuSection` (normative example)

```java
public final class MenuSection extends Section {
    private final List<String> items;
    private int selected = 0;
    private Consumer<Integer> onSelect = i -> {};

    public MenuSection(String... items) { this.items = List.of(items); }
    public MenuSection onSelect(Consumer<Integer> h) { this.onSelect = h; return this; }
    public int selectedIndex() { return selected; }

    @Override protected void render(Canvas c) {
        boolean focused = isFocused();
        for (int i = 0; i < items.size(); i++) {
            Style s = Style.NORMAL;
            if (i == selected) s = focused ? Style.REVERSE : Style.DIM_REVERSE;
            c.put(1, i, items.get(i), s);
        }
    }

    @Override protected KeyResult onKey(KeyEvent k) {
        switch (k.code()) {
            case UP:    selected = Math.max(0, selected - 1); requestRedraw(); return KeyResult.CONSUMED;
            case DOWN:  selected = Math.min(items.size()-1, selected + 1); requestRedraw(); return KeyResult.CONSUMED;
            case ENTER: onSelect.accept(selected); return KeyResult.CONSUMED;
            default:    return KeyResult.UNHANDLED;
        }
    }
}
```

The parked-vs-focused highlight (`DIM_REVERSE` vs `REVERSE`) is required: it is the UX cue that makes multi-pane focus legible.

---

## 9. Chrome ownership (borders/titles)

Borders and titles are owned by a wrapping **`BorderSection`**, not by content Sections. Rationale: the framework can render a *focused* border automatically based on the wrapped child's focus state, and content Sections stay concerned only with content. A `BorderSection` insets its child's bounds by the border thickness and renders the focused/unfocused frame.

(Implementer option: `BorderSection.focusedStyle(...)` / `.title(...)`. A convenience `someSection.bordered("Title")` factory returning a `BorderSection` wrapping it is encouraged.)

---

## 10. End-to-end reference program (must compile & run once implemented)

This is the acceptance scenario: a static left menu (arrow-navigable) driving a right-hand tabbed view, with a header and status bar, plus a live-updating log tab fed from a background thread.

```java
LogSection logs = new LogSection(logStream);        // ScrollSection subtype, follow-tail
MetricsSection metrics = new MetricsSection();
ConfigSection config = new ConfigSection(settings);

TabHost detail = new TabHost()
    .add("Logs", logs)
    .add("Metrics", metrics)
    .add("Config", config);

MenuSection menu = new MenuSection("Logs", "Metrics", "Config")
    .onSelect(detail::select);                       // menu drives the tabs

Section ui = Split.vertical(
    Constraint.fixed(3), new LabelSection("My App").bordered(),
    Constraint.fill(),   Split.horizontal(
        Constraint.fixed(20), menu.bordered("Menu"),
        Constraint.fill(),    detail.bordered()),
    Constraint.fixed(1), new StatusBar("TAB: switch pane  ↑↓: navigate  ←→: tabs  q: quit"));

App.builder()
    .root(ui)
    .focusOrder(menu, detail)
    .navigation(Navigation.spatial())
    .onQuitKey(Key.CHAR /* 'q' */)
    .build()
    .run();
```

Expected behavior:
- `TAB` moves focus between `menu` and `detail`; both repaint, selection highlight dims when parked.
- With `menu` focused: `↑`/`↓` move selection; `ENTER` (or selection, per chosen UX) switches the right-hand tab via `detail.select`.
- With `detail` focused: `←`/`→` switch tabs; `↑`/`↓` scroll the active tab's content (e.g. the log).
- Background `logStream` lines appear live in the Logs tab via `App.post(...)`, repainting only the log section's changed cells.
- Terminal resize reflows all panes; quitting restores the terminal cleanly.

---

## 11. Implementation notes & ordering

Suggested build order (each step independently testable):

1. **Terminal driver**: raw mode, alt-screen, input→`KeyEvent` normalization, size query, restore-on-exit. (May wrap JLine internally.)
2. **Buffers + Canvas**: cell grid, double buffer, diff-and-emit, clipping `Canvas`/`clip`. Unit-test the diff (write known cells, assert minimal emitted ops).
3. **`Section` + `Split` + `Constraint` + layout solver**: render a static nested layout. Test the solver's tiling/rounding invariants.
4. **Run loop + `App` + dirty repaint**: get a static UI on screen, resize-safe.
5. **Focus manager (flat ring) + key dispatch/bubbling**: `TAB` between panes, focus hooks fire.
6. **Built-in Sections**: `MenuSection`, `ScrollSection`/`LogSection`, `LabelSection`, `StatusBar`, `BorderSection`.
7. **`TabHost`** + `FocusContainer` for tab-bar-vs-content.
8. **`App.post` / `schedule`**: thread-safe marshalling; wire the live-log acceptance test.
9. Polish: spatial navigation, quit-key, docs.

### Invariants to assert in tests
- Canvas writes outside bounds are no-ops.
- Layout solver output exactly tiles the available rect (no gaps/overlap) for randomized constraint sets.
- A keystroke unhandled by the focused section bubbles and reaches the focus manager.
- `TabHost.select` fires exactly one `onUnmount` (outgoing) + one `onMount` (incoming), preserves child instances under `preserveState`.
- Off-UI-thread `requestRedraw()` is rejected/guarded; `App.post` is the supported path.
- Idle UI (no dirty sections) emits zero bytes to the terminal per loop iteration.
- `quit()` fully restores terminal state (raw mode off, main screen, cursor shown) even on exception.

---

## 12. Open decisions left to the implementer

These are explicitly delegated; pick sensible defaults and document them:

- Exact tab-bar rendering style (separators, active indicator).
- Whether menu selection drives tab switch on navigation vs. on `ENTER` (default: provide the callback; the reference program wires `onSelect`).
- Frame-rate cap value and input poll timeout.
- `Color` depth beyond 16 ANSI (leave hooks, don't implement).
- Whether `Split` uses raw `Object...` varargs or a typed `Slot`/builder form (spec prefers typed; see §3).