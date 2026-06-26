package com.tonic.vellum.widget;

import com.tonic.vellum.layout.Constraint;
import com.tonic.vellum.style.Style;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    private static String row(RecordingCanvas c, int y) {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < c.width(); x++) {
            sb.append(c.charAt(x, y));
        }
        return sb.toString();
    }

    @Test
    void headerAndRowsAlignByColumn() {
        Table table = new Table()
                .column("Name", Constraint.fixed(5))
                .column("Age", Constraint.fixed(3), Alignment.RIGHT)
                .addRow("Bob", "10")
                .addRow("Al", "9");

        RecordingCanvas c = new RecordingCanvas(8, 3); // 1 header + 2 rows
        table.render(c);

        assertEquals("Name Age", row(c, 0));        // header: Name left, Age right
        assertEquals("Bob   10", row(c, 1));        // 10 right-aligned in the 3-wide column
        assertEquals("Al     9", row(c, 2));
    }

    @Test
    void columnsTileTheWidthWithFill() {
        Table table = new Table()
                .column("A", Constraint.fixed(3))
                .column("B", Constraint.fill())
                .addRow("xx", "yy");
        RecordingCanvas c = new RecordingCanvas(10, 2);
        table.render(c);

        // col A is 3 wide (xx + pad), col B fills the remaining 7 starting at x=3
        assertEquals('x', c.charAt(0, 1));
        assertEquals('y', c.charAt(3, 1));
    }

    @Test
    void selectedRowIsHighlightedAndAccessible() {
        Table table = new Table()
                .column("A", Constraint.fill())
                .addRow("one")
                .addRow("two");
        RecordingCanvas c = new RecordingCanvas(6, 3);
        table.render(c);

        assertArrayEquals(new String[]{"one"}, table.selectedRow());
        assertEquals(Style.DIM_REVERSE, c.styleAt(0, 1)); // selected first row, parked
    }
}
