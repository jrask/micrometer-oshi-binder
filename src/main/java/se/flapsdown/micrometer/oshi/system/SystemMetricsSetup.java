package se.flapsdown.micrometer.oshi.system;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SystemMetricsSetup {

    public enum Flavour {
        TELEGRAF,
        NODE_EXPORTER
    }

    Flavour f;

    public List<MeterBinder> resources = new ArrayList<>();

    public SystemMetricsSetup(Flavour flavour) {
        this.f = flavour;
    }

    public void addBinder(MeterBinder binder) {
        resources.add(binder);
    }

    public void close() {

    }


    public static Builder builder(Flavour f) {
        return new Builder(f);
    }

    public static class Builder {

        SystemMetricsSetup setup;
        Flavour f;

        public Builder (Flavour f) {
            setup = new SystemMetricsSetup(f);
            this.f = f;
        }


        public Builder withProcessorMetrics(Iterable<Tag> tags) {
            if (f == Flavour.TELEGRAF) {
                setup.addBinder(new TelegrafProcessorMetrics(tags));
            } else {
                setup.addBinder(new NodeExporterProcessorMetrics(tags));
            }
            return this;
        }

        public Builder withProcessorMetrics() {
            return withProcessorMetrics(Collections.emptyList());
        }

        public Builder refreshInterval(long duration, TimeUnit unit) {
            return this;
        }

        public SystemMetricsSetup bindTo(MeterRegistry registry) {
            return setup.bindTo(registry);
        }

        public Builder withMemoryMetrics() {
            setup.addBinder(new MemoryMetrics());
            return this;
        }
    }

    private SystemMetricsSetup bindTo(MeterRegistry registry) {
        for (MeterBinder binder : resources) {
            binder.bindTo(registry);
        }
        return this;
    }
}
