package se.flapsdown.micrometer.oshi.system;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.util.Collections;

public class ProcessorMetrics implements MeterBinder {

    private final Iterable<Tag> tags;
    private CentralProcessor processor;

    public ProcessorMetrics() {
        this(Collections.emptyList());
    }

    public ProcessorMetrics(Iterable<Tag> tags) {
        this.tags = tags;
        processor = new SystemInfo().getHardware().getProcessor();
    }

    @Override
    public void bindTo(MeterRegistry meterRegistry) {

        double[] systemLoadAverage = processor.getSystemLoadAverage(3);

        Gauge.builder("system.load.1m", () -> systemLoadAverage[0])
                .tags(tags)
                .description("System load 1m")
                .register(meterRegistry);

        Gauge.builder("system.load.5m", () -> systemLoadAverage[1])
                .tags(tags)
                .description("System load 5m")
                .register(meterRegistry);

        Gauge.builder("system.load.15m", () -> systemLoadAverage[2])
                .tags(tags)
                .description("System load 15m")
                .register(meterRegistry);

//        An array of 7 long values representing time spent in User, Nice,
//     *         System, Idle, IOwait, IRQ, SoftIRQ, and Steal states.
        long[] systemCpuLoadTicks = processor.getSystemCpuLoadTicks();
        System.out.println("User : " + systemCpuLoadTicks[0]);
        System.out.println("Nice : " + systemCpuLoadTicks[1]);
        System.out.println("System : " + systemCpuLoadTicks[2]);
        System.out.println("Idle : " + systemCpuLoadTicks[3]);
        System.out.println("IOWait : " + systemCpuLoadTicks[4]);
        System.out.println("IRQ : " + systemCpuLoadTicks[5]);
        System.out.println("SorfIRq : " + systemCpuLoadTicks[6]);
    }
}
