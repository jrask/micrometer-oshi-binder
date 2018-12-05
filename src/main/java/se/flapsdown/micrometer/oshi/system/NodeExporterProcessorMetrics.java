package se.flapsdown.micrometer.oshi.system;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.Collections;

public class NodeExporterProcessorMetrics extends ProcessorMetrics implements MeterBinder {

    public NodeExporterProcessorMetrics() {
        this(Collections.emptyList());
    }

    public NodeExporterProcessorMetrics(Iterable<Tag> tags) {
        super(tags);
    }


    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        cpuUsageInSeconds(meterRegistry);
    }

    private void cpuUsageInSeconds(MeterRegistry meterRegistry) {

        Gauge.builder("node_cpu_seconds_total",  "userTimeSecondsTotal", super::getCpuMetricAsDouble)
            .tags(tags)
            .tag("mode", "user")
            .description("User cpu usage in percent")
            .register(meterRegistry);
    }
}
