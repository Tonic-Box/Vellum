# Layout

[Back to index](index.md)

Layout is a Section. `com.tonic.vellum.layout.Split` divides its bounds among children
along one axis by constraints. Nesting Splits produces arbitrary layouts.

## Split

```java
// Typed varargs
Section ui = Split.vertical(
        Slot.of(Constraint.fixed(3), header),
        Slot.of(Constraint.fill(), body),
        Slot.of(Constraint.fixed(1), statusBar));

// Fluent
Split menu = Split.horizontal()
        .add(Constraint.fixed(20), sidebar)
        .add(Constraint.fill(), detail);
```

| Method | Description |
|---|---|
| `static Split horizontal(Slot...)` | Split along the x axis. |
| `static Split vertical(Slot...)` | Split along the y axis. |
| `Split add(Constraint size, Section child)` | Append a constrained child (at construction). |

The split direction applies constraints to width (horizontal) or height (vertical); the
cross axis always gets the full extent. Children tile the bounds exactly, with no gaps or
overlap.

## Slot

`com.tonic.vellum.layout.Slot` pairs a constraint with a child.

| Method | Description |
|---|---|
| `static Slot of(Constraint, Section)` | Create a slot. |
| `Constraint constraint()` | The constraint. |
| `Section section()` | The child. |

## Constraint

`com.tonic.vellum.layout.Constraint` sizes one child along the main axis.

| Factory | Meaning |
|---|---|
| `fixed(int cells)` | Exactly N cells. |
| `percent(int pct)` | A percentage of the original available extent. |
| `fill()` | An equal share of the remaining space. |
| `fill(int weight)` | A weighted share of the remaining space. |
| `min(int cells)` | At least N, grows into remaining space. |
| `max(int cells)` | At most N, grows up to that cap. |

Accessors: `Kind kind()` and `int value()`. `Kind` is one of `FIXED`, `PERCENT`, `FILL`,
`MIN`, `MAX`.

## Solver

Fixed and percent sizes form each child's base. Remaining space is distributed across
`fill`, `min`, and `max` by weight, respecting `min` floors and `max` caps. Any rounding
remainder goes to the last flexible child, so the children always tile the extent exactly.

`com.tonic.vellum.layout.LayoutSolver` exposes this directly:

```java
Rect[] rects = LayoutSolver.solve(available, constraints, Axis.VERTICAL);
```

| Method | Description |
|---|---|
| `static Rect[] solve(Rect available, List<Constraint>, Axis)` | Returns one Rect per constraint, tiling `available`. |

`com.tonic.vellum.layout.Axis` is `HORIZONTAL` or `VERTICAL`.

See [Sections](sections.md) for writing your own container, and [Rendering](rendering.md)
for `Rect`.
