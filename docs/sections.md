# Sections

[Back to index](index.md)

`com.tonic.vellum.Section` is the single universal unit. Every visible thing is a Section;
containers are Sections that hold Sections. The framework assigns bounds, clips a
[Canvas](rendering.md) to them, routes keys, and repaints the dirty subtree.

## Methods to override

| Method | Default | Description |
|---|---|---|
| `void render(Canvas canvas)` | abstract | Draw into a canvas clipped to this Section's bounds, in local coordinates. |
| `KeyResult onKey(KeyEvent key)` | `UNHANDLED` | Handle a key. Return `CONSUMED` to stop propagation, `UNHANDLED` to bubble. |
| `void onMount()` | no-op | Entered the live render path (shown). |
| `void onUnmount()` | no-op | Left the live render path (hidden). |
| `void onFocusGained()` | no-op | Received focus. |
| `void onFocusLost()` | no-op | Lost focus. |
| `void onResize(Rect newBounds)` | no-op | Bounds changed (layout or terminal resize). |
| `Point cursor()` | `null` | Text cursor position in local coordinates, or null to hide it. |

All hooks run on the UI thread. `cursor()` is queried once per repaint for the focused
section only (the deepest node on the focus path); editable widgets override it to place the
hardware cursor. See `TextInput` in [Widgets](widgets.md).

## Public methods

| Method | Description |
|---|---|
| `void requestRedraw()` | Mark dirty. UI-thread-only. |
| `boolean isFocused()` | True if on the focus path. |
| `Rect bounds()` | Current bounds. |
| `Section parent()` | Parent, or null for the root. |
| `Section bordered()` | Wrap in a [BorderSection](widgets.md). |
| `Section bordered(String title)` | Wrap in a titled border. |

## A custom Section

```java
public final class Counter extends Section {
    private int n;

    public void increment() {
        n++;
        requestRedraw();
    }

    @Override
    protected void render(Canvas canvas) {
        canvas.put(0, 0, "Count: " + n);
    }

    @Override
    protected KeyResult onKey(KeyEvent key) {
        if (key.is(Key.ENTER)) {
            increment();
            return KeyResult.CONSUMED;
        }
        return KeyResult.UNHANDLED;
    }
}
```

The canvas is already clipped to the Section's bounds; out-of-bounds writes are discarded.
Leaf Sections have their rect cleared before `render`, so unused cells stay blank.

## Lifecycle order

At `run()`, the live tree mounts top-down (`onMount`). Layout cascades from the root via
`onResize`. A [TabHost](widgets.md) mounts only its active child; inactive tabs mount on
first selection. On exit, the tree unmounts.

## Writing a container

A container overrides `children()` and lays out its children in `onResize`. The protected
methods below operate on a child passed as an argument.

| Method | Description |
|---|---|
| `List<Section> children()` | The children on the live render path. Default: none. |
| `void place(Section child, Rect bounds)` | Assign a child's bounds; cascades `onResize` and marks dirty when bounds change. |
| `void mount(Section child)` | Bring a child subtree onto the live path (fires `onMount`). |
| `void unmount(Section child)` | Remove a child subtree (fires `onUnmount`); the instance is retained. |
| `void redrawSubtree(Section target)` | Mark a child subtree dirty (e.g. when re-showing it). |

```java
public final class Padding extends Section {
    private final Section child;
    private final int pad;

    public Padding(Section child, int pad) {
        this.child = child;
        this.pad = pad;
    }

    @Override
    protected List<Section> children() {
        return Collections.singletonList(child);
    }

    @Override
    protected void onResize(Rect newBounds) {
        place(child, newBounds.inset(pad));
    }

    @Override
    protected void render(Canvas canvas) {
        // container draws only its own chrome; the child draws its interior
    }
}
```

`children()` drives every framework tree walk (layout, mount, repaint, focus). A container
returning a subset of its children (as `TabHost` does) keeps the rest off the live path.

See [Layout](layout.md) for the built-in `Split` container and [Widgets](widgets.md) for
`TabHost` and `BorderSection`.
