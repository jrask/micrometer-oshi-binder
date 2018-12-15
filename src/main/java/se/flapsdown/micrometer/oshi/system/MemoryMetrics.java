package se.flapsdown.micrometer.oshi.system;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

import java.util.Collections;

public class MemoryMetrics implements MeterBinder {

    private final GlobalMemory memory;
    private final Iterable<Tag> tags;

    public MemoryMetrics() {
        this(Collections.emptyList());
    }

    public MemoryMetrics(Iterable<Tag> tags) {
        this.memory = new SystemInfo().getHardware().getMemory();

        this.tags = tags;
    }

    public void bindTo(MeterRegistry meterRegistry) {

        Gauge.builder("system.memory.available", () -> memory.getAvailable())
                .tags(tags)
                .description("Memory available")
                .register(meterRegistry);


        Gauge.builder("system.memory.total", () -> memory.getTotal())
                .tags(tags)
                .description("Memory available")
                .register(meterRegistry);


        Gauge.builder("system.memory.used", () -> memory.getTotal() - memory.getAvailable())
                .tags(tags)
                .description("Memory available")
                .register(meterRegistry);

        Gauge.builder("system.memory.swap.total", () -> memory.getSwapTotal())
                .tags(tags)
                .description("Memory Swap total")
                .register(meterRegistry);

        Gauge.builder("system.memory.swap.used", () -> memory.getSwapUsed())
                .tags(tags)
                .description("Memory Swap used")
                .register(meterRegistry);

    }


}
