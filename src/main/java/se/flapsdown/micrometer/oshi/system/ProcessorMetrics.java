package se.flapsdown.micrometer.oshi.system;

import io.micrometer.core.instrument.Tag;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public abstract class ProcessorMetrics  {


    protected final Iterable<Tag> tags;

    protected ProcessorMetrics.CpuMetrics cpuMetrics = null;

    protected ProcessorMetrics(Iterable<Tag> tags) {
        this.tags = tags;
        this.cpuMetrics = new CpuMetrics(new SystemInfo().getHardware().getProcessor());
    }

    /**
     * Gives a chances to refresh without an external counter
     */
    public double getCpuMetricAsDouble(String type) {
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
        } else if (type.equals("userTimeSecondsTotal")) {
            return cpuMetrics.userTimeSecondsTotal;
        }
        return 0;
    }

    protected static class CpuMetrics {

        private static final long TICKS_PER_SEC = 10_000_000;

        private long refreshTime = 0;
        final CentralProcessor processor;

        private long prevTicks[] = null;
        private long userTimeSecondsTotal;

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
        private long niceTimeInSecondsTotal;
        private long sysTimeInSecondsTotal;



        public CpuMetrics(CentralProcessor processor) {
            this.processor = processor;
        }

        // publish() is probably invoked on a single thread?
        public void refresh() {
            System.out.println("refresh() 1");
            if (prevTicks == null) {
                prevTicks = processor.getSystemCpuLoadTicks();
                return;
            }

            // quick and dirty to prevent multiple refreshes
            if (System.currentTimeMillis() - refreshTime < 2000) {
                return;
            }
            System.out.println("refresh()");
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

            System.out.println(ticks[CentralProcessor.TickType.USER.getIndex()]);

           // processor.getProcessorCpuLoadBetweenTicks()
            this.userTimeSecondsTotal = ticks[CentralProcessor.TickType.USER.getIndex()] / 1000;
            this.niceTimeInSecondsTotal = TICKS_PER_SEC * nice;
            this.sysTimeInSecondsTotal = TICKS_PER_SEC * nice;

            refreshTime = System.currentTimeMillis();

            //System.out.format("User: %.1f%%\n", this.user);
            //System.out.format("System: %.1f%%\n", this.sys);
            //System.out.format("Nice: %.1f%%\n", this.nice);
            //System.out.format("Idle: %.1f%%\n", this.idle);

            prevTicks = ticks;
        }
    }
}
