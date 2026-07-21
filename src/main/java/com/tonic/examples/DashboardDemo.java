package com.tonic.examples;

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

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * End-to-end demo: a left menu driving a right-hand tabbed view, with a header, status bar, live-updating log tab, and timer-driven metrics tab.
 */
public final class DashboardDemo
{
    /**
     * A custom section showing a live uptime counter, ticked by a scheduled task.
     */
    static final class MetricsSection extends Section
    {
        private int seconds;

        void tick()
        {
            seconds++;
            requestRedraw();
        }

        @Override
        protected void render(Canvas canvas)
        {
            canvas.put(1, 1, "Uptime: " + seconds + "s");
            canvas.put(1, 3, "Status: OK", Style.NORMAL.fg(Color.GREEN).bold(true));
            canvas.put(1, 5, "Requests/s: " + (40 + seconds % 13));
        }
    }

    private DashboardDemo()
    {
    }

    public static void main(String[] args)
    {
        LogSection logs = new LogSection();
        MetricsSection metrics = new MetricsSection();
        TextSection config = new TextSection("host        = localhost\n" + "port        = 8080\n" + "log.level   = INFO\n" + "workers     = 4\n" + "retry.limit = 3");

        TabHost detail = new TabHost()
                .add("Logs", logs)
                .add("Metrics", metrics)
                .add("Config", config);

        MenuSection menu = new MenuSection("Logs", "Metrics", "Config")
                .onSelect(detail::select);

        Section ui = Split.vertical(Slot.of(Constraint.fixed(3), new LabelSection("Vellum Dashboard").alignment(Alignment.CENTER).bordered()), Slot.of(Constraint.fill(), Split.horizontal(Slot.of(Constraint.fixed(20), menu.bordered("Menu")), Slot.of(Constraint.fill(), detail.bordered()))), Slot.of(Constraint.fixed(1), new StatusBar("TAB: switch pane   up/down: navigate   left/right: tabs   q: quit")));

        App app = App.builder()
                .root(ui)
                .focusOrder(menu, detail)
                .navigation(Navigation.spatial())
                .onQuitKey('q')
                .build();

        AtomicBoolean running = new AtomicBoolean(true);
        Thread feeder = startLogFeeder(app, logs, running);
        app.scheduleAtFixedRate(Duration.ofSeconds(1), Duration.ofSeconds(1), metrics::tick);

        try
        {
            app.run();
        }
        finally
        {
            running.set(false);
            feeder.interrupt();
        }
    }

    private static Thread startLogFeeder(App app, LogSection logs, AtomicBoolean running)
    {
        Thread feeder = new Thread(() ->
        {
            int n = 0;
            while (running.get())
            {
                try
                {
                    Thread.sleep(600);
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    break;
                }
                final int id = n++;
                app.post(() -> logs.append(String.format("[%05d] worker-%d processed request in %dms", id, id % 4, 12 + id % 40)));
            }
        }, "log-feeder");
        feeder.setDaemon(true);
        feeder.start();
        return feeder;
    }
}
