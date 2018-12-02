package se.flapsdown.micrometer.oshi.system;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.util.Collections;

public class ProcessorMetricsAsPercentage implements MeterBinder {

    private final Iterable<Tag> tags;

    private CpuMetrics cpuMetrics = null;

    public ProcessorMetricsAsPercentage() {
        this(Collections.emptyList());
    }

    public ProcessorMetricsAsPercentage(Iterable<Tag> tags) {
        this.tags = tags;
        cpuMetrics = new CpuMetrics(new SystemInfo().getHardware().getProcessor());
    }

    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        systemLoad(meterRegistry);
        cpuUsage(meterRegistry);
    }

    private void cpuUsage(MeterRegistry meterRegistry) {

        Gauge.builder("system.cpu.cores", () -> cpuMetrics.processor.getPhysicalProcessorCount())
            .tags(tags)
            .description("User cpu usage in percent")
            .register(meterRegistry);

        Gauge.builder("system.cpu.user.pct", "user", this::getCpuMetric)
            .tags(tags)
            .description("User cpu usage in percent")
            .register(meterRegistry);

        Gauge.builder("system.cpu.system.pct", "sys", this::getCpuMetric)
            .tags(tags)
            .description("System cpu usage in percent")
            .register(meterRegistry);

        Gauge.builder("system.cpu.idle.pct", "idle", this::getCpuMetric)
            .tags(tags)
            .description("Idle cpu usage in percent")
            .register(meterRegistry);

        Gauge.builder("system.cpu.nice.pct", "nice", this::getCpuMetric)
            .tags(tags)
            .description("Nice cpu usage in percent")
            .register(meterRegistry);
    }

    private void systemLoad(MeterRegistry meterRegistry) {


        Gauge.builder("system.load.1m", "load1m", this::getCpuMetric )
            .tags(tags)
            .description("System load 1m")
            .register(meterRegistry);

        Gauge.builder("system.load.5m", "load5m", this::getCpuMetric)
            .tags(tags)
            .description("System load 5m")
            .register(meterRegistry);

        Gauge.builder("system.load.15m", "load15m", this::getCpuMetric)
            .tags(tags)
            .description("System load 15m")
            .register(meterRegistry);
    }



    private double getCpuMetric(String type) {
        cpuMetrics.refresh();
        if (type.equals("user")) {
            return cpuMetrics.user;
        } else if (type.equals("nice")) {
            return cpuMetrics.nice;
        } else if (type.equals("sys")) {
            return cpuMetrics.sys;
        } else if (type.equals("idle")) {
            return cpuMetrics.idle;
        } else if (type.equals("iowait")) {
            return cpuMetrics.iowait;
        } else if (type.equals("irq")) {
            return cpuMetrics.irq;
        } else if (type.equals("softirq")) {
            return cpuMetrics.softirq;
        } else if (type.equals("steal")) {
            return cpuMetrics.steal;
        } else if (type.equals("load1m")) {
            return cpuMetrics.load1m;
        } else if (type.equals("load5m")) {
            return cpuMetrics.load5m;
        } else if (type.equals("load15m")) {
            return cpuMetrics.load15m;
        }
        return 0;
    }

    static class CpuMetrics {

        long refreshTime = 0;
        final CentralProcessor processor;

        long prevTicks[] = null;

        double user;
        double nice;
        double sys;
        double idle;
        double iowait;
        double irq;
        double softirq;
        double steal;

        double load1m;
        double load5m;
        double load15m;

        public CpuMetrics(CentralProcessor processor) {
            this.processor = processor;
        }

        // publish() is probably invoked on a single thread?
        public void refresh() {

            if (prevTicks == null) {
                prevTicks = processor.getSystemCpuLoadTicks();
                return;
            }

            // quick and dirty to prevent multiple refreshes
            if (System.currentTimeMillis() - refreshTime < 2000) {
                return;
            }
            double[] systemLoadAverage = processor.getSystemLoadAverage(3);
            load1m = systemLoadAverage[0] < 0 ? 0 : systemLoadAverage[0];
            load5m = systemLoadAverage[1] < 0 ? 0 : systemLoadAverage[1];
            load15m = systemLoadAverage[2] < 0 ? 0 : systemLoadAverage[2];

            long[] ticks = processor.getSystemCpuLoadTicks();
            long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
            long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
            long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
            long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
            long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
            long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
            long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
            long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
            long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;

            this.user    = 100d * user    / totalCpu;
            this.nice    = 100d * nice    / totalCpu;
            this.sys     = 100d * sys     / totalCpu;
            this.idle    = 100d * idle    / totalCpu;
            this.iowait  = 100d * iowait  / totalCpu;
            this.irq     = 100d * irq     / totalCpu;
            this.softirq = 100d * softirq / totalCpu;
            this.steal   = 100d * steal   / totalCpu;

            refreshTime = System.currentTimeMillis();

            //System.out.format("User: %.1f%%\n", this.user);
            //System.out.format("System: %.1f%%\n", this.sys);
            //System.out.format("Nice: %.1f%%\n", this.nice);
            //System.out.format("Idle: %.1f%%\n", this.idle);

            prevTicks = ticks;
        }
    }
}
