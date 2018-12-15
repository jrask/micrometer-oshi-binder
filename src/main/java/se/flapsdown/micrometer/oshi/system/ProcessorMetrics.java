package se.flapsdown.micrometer.oshi.system;

import com.sun.jna.Platform;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

import java.util.Collections;

import static se.flapsdown.micrometer.oshi.system.ProcessorMetrics.ProcessorMetric.*;

public class ProcessorMetrics  implements MeterBinder {

    protected static final Logger LOG = LoggerFactory.getLogger(ProcessorMetrics.class);

    public enum ProcessorMetric {
        USER_PERCENT,
        NICE_PERCENT,
        SYSTEM_PERCENT,
        IDLE_PERCENT,
        IOWAIT_PERCENT,
        IRQ_PERCENT,
        SOFTIRQ_PERCENT,
        STEAL_PERCENT,

        LOAD_1M,
        LOAD_5M,
        LOAD_15M,

        USER_TIME_SECONDS_TOTAL,
        NICE_TIME_SECONDS_TOTAL,
        SYS_TIME_SECONDS_TOTAL,
        IDLE_TIME_SECONDS_TOTAL,
        IOWAIT_TIME_IN_SECONDS_TOTAL,
        IRQ_TIME_IN_SECONDS_TOTAL,
        SOFTIRQ_TIME_IN_SECONDS_TOTAL,
        STEAL_TIME_IN_SECONDS_TOTAL
    }



    protected final Iterable<Tag> tags;

    protected final CalculatedCpuMetrics calculatedCpuMetrics;

    static long TICKS_PER_SEC = 1;
    static {
        if (Platform.isMac() || Platform.isLinux()) {
            TICKS_PER_SEC = ParseUtil.parseLongOrDefault(ExecutingCommand.getFirstAnswer("getconf CLK_TCK"),
                100L);
        } else {
            LOG.info("Ticks per second is unsupported on this platform");
        }
    }

    protected boolean isCpuTotalSecondsSupported() {
        return TICKS_PER_SEC > 1;
    }

    public ProcessorMetrics() {
        this(Collections.emptyList());
    }

    public ProcessorMetrics(Iterable<Tag> tags) {
        this.tags = tags;
        this.calculatedCpuMetrics = new CalculatedCpuMetrics(new SystemInfo().getHardware().getProcessor());
    }

    /**
     *
     */
    public double getCpuMetricAsDouble(ProcessorMetric type) {
        calculatedCpuMetrics.refresh();

        switch (type) {
            case USER_PERCENT: return calculatedCpuMetrics.user;
            case NICE_PERCENT: return calculatedCpuMetrics.nice;
            case SYSTEM_PERCENT: return calculatedCpuMetrics.sys;
            case IDLE_PERCENT: return calculatedCpuMetrics.idle;
            case IOWAIT_PERCENT: return calculatedCpuMetrics.iowait;
            case IRQ_PERCENT: return calculatedCpuMetrics.irq;
            case SOFTIRQ_PERCENT: return calculatedCpuMetrics.softirq;
            case STEAL_PERCENT: return calculatedCpuMetrics.steal;

            case LOAD_1M: return calculatedCpuMetrics.load1m;
            case LOAD_5M: return calculatedCpuMetrics.load5m;
            case LOAD_15M: return calculatedCpuMetrics.load15m;

            case USER_TIME_SECONDS_TOTAL: return calculatedCpuMetrics.userTimeSecondsTotal;
            case NICE_TIME_SECONDS_TOTAL: return calculatedCpuMetrics.niceTimeInSecondsTotal;
            case SYS_TIME_SECONDS_TOTAL: return calculatedCpuMetrics.sysTimeInSecondsTotal;
            case IDLE_TIME_SECONDS_TOTAL: return calculatedCpuMetrics.idleTimeInSecondsTotal;
            case IOWAIT_TIME_IN_SECONDS_TOTAL: return calculatedCpuMetrics.iowaitTimeInSecondsTotal;
            case IRQ_TIME_IN_SECONDS_TOTAL: return calculatedCpuMetrics.irqTimeInSecondsTotal;
            case SOFTIRQ_TIME_IN_SECONDS_TOTAL: return calculatedCpuMetrics.softirqTimeInSecondsTotal;
            case STEAL_TIME_IN_SECONDS_TOTAL: return calculatedCpuMetrics.stealTimeInSecondsTotal;
            default: return 0;
        }
    }

    protected static class CalculatedCpuMetrics {

        private long refreshTime = 0;
        final CentralProcessor processor;

        private long prevTicks[] = null;


        private double user;
        private double nice;
        private double sys;
        private double idle;
        private double iowait;
        private double irq;
        private double softirq;
        private double steal;

        private double load1m;
        private double load5m;
        private double load15m;

        private long userTimeSecondsTotal;
        private long niceTimeInSecondsTotal;
        private long sysTimeInSecondsTotal;
        private long idleTimeInSecondsTotal;
        private long iowaitTimeInSecondsTotal;
        private long irqTimeInSecondsTotal;
        private long softirqTimeInSecondsTotal;
        private long stealTimeInSecondsTotal;

        public CalculatedCpuMetrics(CentralProcessor processor) {
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


           // processor.getProcessorCpuLoadBetweenTicks()
            this.userTimeSecondsTotal = ticks[CentralProcessor.TickType.USER.getIndex()] / TICKS_PER_SEC;
            this.niceTimeInSecondsTotal = ticks[CentralProcessor.TickType.NICE.getIndex()] / TICKS_PER_SEC;
            this.sysTimeInSecondsTotal = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] / TICKS_PER_SEC;
            this.idleTimeInSecondsTotal = ticks[CentralProcessor.TickType.IDLE.getIndex()] / TICKS_PER_SEC;
            this.iowaitTimeInSecondsTotal = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] / TICKS_PER_SEC;
            this.irqTimeInSecondsTotal = ticks[CentralProcessor.TickType.IRQ.getIndex()] / TICKS_PER_SEC;
            this.softirqTimeInSecondsTotal = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] / TICKS_PER_SEC;
            this.stealTimeInSecondsTotal = ticks[CentralProcessor.TickType.STEAL.getIndex()] / TICKS_PER_SEC;

            refreshTime = System.currentTimeMillis();

            //System.out.format("User: %.1f%%\n", this.user);
            //System.out.format("System: %.1f%%\n", this.sys);
            //System.out.format("Nice: %.1f%%\n", this.nice);
            //System.out.format("Idle: %.1f%%\n", this.idle);

            prevTicks = ticks;
        }
    }


    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        cpuUsageInSeconds(meterRegistry);
        cpuUsagePercent(meterRegistry);
        systemLoad(meterRegistry);
    }


    private void cpuUsagePercent(MeterRegistry meterRegistry) {



        Gauge.builder("system.cpu.usage", ProcessorMetric.USER_PERCENT, this::getCpuMetricAsDouble)
            .tags(tags)
            .tag("mode", "user")
            .description("User cpu usage in percent")
            .register(meterRegistry);

        Gauge.builder("system.cpu.usage", ProcessorMetric.SYSTEM_PERCENT, this::getCpuMetricAsDouble)
            .tags(tags)
            .tag("mode", "system")
            .description("System cpu usage in percent")
            .register(meterRegistry);

        Gauge.builder("system.cpu.usage",  ProcessorMetric.IDLE_PERCENT, this::getCpuMetricAsDouble)
            .tags(tags)
            .tag("mode", "idle")
            .description("Idle cpu usage in percent")
            .register(meterRegistry);

        Gauge.builder("system.cpu.usage",  ProcessorMetric.NICE_PERCENT, this::getCpuMetricAsDouble)
            .tags(tags)
            .tag("mode", "nice")
            .description("Nice cpu usage in percent")
            .register(meterRegistry);

        Gauge.builder("system.cpu.usage",  ProcessorMetric.IRQ_PERCENT, this::getCpuMetricAsDouble)
            .tags(tags)
            .tag("mode", "irq")
            .description("IRQ cpu usage in percent")
            .register(meterRegistry);

        Gauge.builder("system.cpu.usage",  ProcessorMetric.SOFTIRQ_PERCENT, this::getCpuMetricAsDouble)
            .tags(tags)
            .tag("mode", "irq")
            .description("SOFTIRQ cpu usage in percent")
            .register(meterRegistry);
    }

    private void systemLoad(MeterRegistry meterRegistry) {

        int cpuCount = calculatedCpuMetrics.processor.getPhysicalProcessorCount();

        Gauge.builder("system.load.1m",  ProcessorMetric.LOAD_1M, this::getCpuMetricAsDouble )
            .tags(tags)
            .tag("n_cpus", String.valueOf(cpuCount))
            .description("System load 1m")
            .register(meterRegistry);

        Gauge.builder("system.load.5m",  ProcessorMetric.LOAD_5M, this::getCpuMetricAsDouble)
            .tags(tags)
            .tag("n_cpus", String.valueOf(cpuCount))
            .description("System load 5m")
            .register(meterRegistry);

        Gauge.builder("system.load.5m",  ProcessorMetric.LOAD_15M, this::getCpuMetricAsDouble)
            .tags(tags)
            .tag("n_cpus", String.valueOf(cpuCount))
            .description("System load 15m")
            .register(meterRegistry);
    }


    private void cpuUsageInSeconds(MeterRegistry meterRegistry) {

        if (isCpuTotalSecondsSupported()) {

            FunctionCounter.builder("system.cpu.seconds.total", USER_TIME_SECONDS_TOTAL, this::getCpuMetricAsDouble)
                .tags(tags)
                .tag("mode", "user")
                .description("User cpu usage in percent")
                .register(meterRegistry);

            FunctionCounter.builder("system.cpu.seconds.total", IDLE_TIME_SECONDS_TOTAL, this::getCpuMetricAsDouble)
                .tags(tags)
                .tag("mode", "idle")
                .description("User cpu idle in percent")
                .register(meterRegistry);

            FunctionCounter.builder("system.cpu.seconds.total", SYS_TIME_SECONDS_TOTAL, this::getCpuMetricAsDouble)
                .tags(tags)
                .tag("mode", "system")
                .description("User cpu usage in percent")
                .register(meterRegistry);

            FunctionCounter.builder("system.cpu.seconds.total", IRQ_TIME_IN_SECONDS_TOTAL, this::getCpuMetricAsDouble)
                .tags(tags)
                .tag("mode", "irq")
                .description("User cpu irq in percent")
                .register(meterRegistry);

            FunctionCounter.builder("system.cpu.seconds.total", SOFTIRQ_TIME_IN_SECONDS_TOTAL, this::getCpuMetricAsDouble)
                .tags(tags)
                .tag("mode", "softirq")
                .description("User cpu softirq in percent")
                .register(meterRegistry);

            FunctionCounter.builder("system.cpu.seconds.total", STEAL_TIME_IN_SECONDS_TOTAL, this::getCpuMetricAsDouble)
                .tags(tags)
                .tag("mode", "steal")
                .description("User cpu steal in percent")
                .register(meterRegistry);

        } else {
            LOG.info("Cunulative cpu seconds counter is unsupported on this platform");
        }
    }
}
