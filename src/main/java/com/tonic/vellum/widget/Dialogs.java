package com.tonic.vellum.widget;

import com.tonic.vellum.App;
import com.tonic.vellum.CharWidth;
import com.tonic.vellum.OverlayHandle;
import com.tonic.vellum.Placement;
import com.tonic.vellum.Section;
import com.tonic.vellum.layout.Constraint;

import java.util.function.Consumer;

/**
 * Modal dialog helpers built on {@code App.openOverlay} and the form widgets. Each opens a
 * centered, bordered {@link Form} and returns its {@link OverlayHandle}. The composing
 * widgets ({@link Button}, {@link Form}, {@link TextInput}, {@link LabelSection}) are public,
 * so fully custom dialogs can be built the same way without these helpers.
 */
public final class Dialogs {

    private Dialogs() {}

    /** A message with an OK button. Closes on OK or ESC. */
    public static OverlayHandle alert(App app, String message) {
        OverlayHandle[] handle = new OverlayHandle[1];
        Button ok = new Button("OK").onActivate(() -> handle[0].close());

        Form form = new Form();
        form.addStatic(new LabelSection(message).alignment(Alignment.CENTER), 1);
        form.addField(ok);
        form.onCancel(() -> handle[0].close());

        handle[0] = open(app, form, width(message, 12), 4);
        return handle[0];
    }

    /** A message with Yes/No. Yes runs {@code onYes} then closes; No/ESC just close. */
    public static OverlayHandle confirm(App app, String message, Runnable onYes) {
        OverlayHandle[] handle = new OverlayHandle[1];
        Button yes = new Button("Yes").onActivate(() -> { onYes.run(); handle[0].close(); });
        Button no = new Button("No").onActivate(() -> handle[0].close());

        Form buttons = Form.row();
        buttons.addField(yes, Constraint.fill()).addField(no, Constraint.fill());

        Form form = new Form();
        form.addStatic(new LabelSection(message).alignment(Alignment.CENTER), 1);
        form.addField(buttons);
        form.onCancel(() -> handle[0].close());

        handle[0] = open(app, form, width(message, 18), 4);
        return handle[0];
    }

    /** A title with a text field and OK/Cancel. OK (or Enter in the field) submits then closes. */
    public static OverlayHandle prompt(App app, String title, Consumer<String> onSubmit) {
        OverlayHandle[] handle = new OverlayHandle[1];
        TextInput input = new TextInput();
        Runnable submit = () -> { onSubmit.accept(input.text()); handle[0].close(); };
        input.onSubmit(text -> submit.run());

        Button ok = new Button("OK").onActivate(submit);
        Button cancel = new Button("Cancel").onActivate(() -> handle[0].close());
        Form buttons = Form.row();
        buttons.addField(ok, Constraint.fill()).addField(cancel, Constraint.fill());

        Form form = new Form();
        form.addStatic(new LabelSection(title), 1);
        form.addField(input);
        form.addField(buttons);
        form.onCancel(() -> handle[0].close());

        handle[0] = open(app, form, width(title, 30), 5);
        return handle[0];
    }

    private static OverlayHandle open(App app, Form form, int width, int height) {
        Section content = form.bordered();
        return app.openOverlay(content, Placement.centered(width, height), form);
    }

    private static int width(String text, int min) {
        return Math.max(min, CharWidth.width(text) + 4);
    }
}
