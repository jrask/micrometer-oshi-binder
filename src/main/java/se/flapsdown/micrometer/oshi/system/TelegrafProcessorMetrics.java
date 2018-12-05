package se.flapsdown.micrometer.oshi.system;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.Collections;

public class TelegrafProcessorMetrics extends ProcessorMetrics implements MeterBinder {


    public TelegrafProcessorMetrics() {
        this(Collections.emptyList());
    }

    public TelegrafProcessorMetrics(Iterable<Tag> tags) {
        super(tags);
        System.out.println(" ->ß");
    }

    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        System.out.println("bindTo");
        systemLoad(meterRegistry);
        cpuUsagePercent(meterRegistry);
    }


    private void cpuUsagePercent(MeterRegistry meterRegistry) {



        Gauge.builder("cpu_usage_user", "user", super::getCpuMetricAsDouble)
            .tags(tags)
            .tag("cpu", "cpu-total")
            .description("User cpu usage in percent")
            .register(meterRegistry);

        Gauge.builder("cpu_usage_system", "sys", super::getCpuMetricAsDouble)
            .tags(tags)
            .tag("cpu", "cpu-total")
            .description("System cpu usage in percent")
            .register(meterRegistry);

        Gauge.builder("cpu_usage_idle",  "idle", super::getCpuMetricAsDouble)
            .tags(tags)
            .tag("cpu", "cpu-total")
            .description("Idle cpu usage in percent")
            .register(meterRegistry);

        Gauge.builder("cpu_usage_nice",  "nice", super::getCpuMetricAsDouble)
            .tags(tags)
            .tag("cpu", "cpu-total")
            .description("Nice cpu usage in percent")
            .register(meterRegistry);

        Gauge.builder("cpu_usage_irq",  "irq", super::getCpuMetricAsDouble)
            .tags(tags)
            .tag("cpu", "cpu-total")
            .description("IRQ cpu usage in percent")
            .register(meterRegistry);

        Gauge.builder("cpu_usage_softirq",  "softirq", super::getCpuMetricAsDouble)
            .tags(tags)
            .tag("cpu", "cpu-total")
            .description("SOFTIRQ cpu usage in percent")
            .register(meterRegistry);
    }

    private void systemLoad(MeterRegistry meterRegistry) {

        int cpuCount = cpuMetrics.processor.getPhysicalProcessorCount();

        Gauge.builder("system_load1",  "load1m", super::getCpuMetricAsDouble )
            .tags(tags)
            .tag("n_cpus", String.valueOf(cpuCount))
            .description("System load 1m")
            .register(meterRegistry);

        Gauge.builder("system_load5m",  "load5m", super::getCpuMetricAsDouble)
            .tags(tags)
            .tag("n_cpus", String.valueOf(cpuCount))
            .description("System load 5m")
            .register(meterRegistry);

        Gauge.builder("system_load15m",  "load15m", super::getCpuMetricAsDouble)
            .tags(tags)
            .tag("n_cpus", String.valueOf(cpuCount))
            .description("System load 15m")
            .register(meterRegistry);
    }

}
