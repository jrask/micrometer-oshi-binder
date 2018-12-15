package se.flapsdown.micrometer.oshi.system;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import oshi.util.FormatUtil;

import java.util.Collections;

public class NetworkMetrics implements MeterBinder {

    private final Iterable<Tag> tags;
    private final NetworkIF[] networks;
    private final CachedNetworkStats networkStats = new CachedNetworkStats();

    public NetworkMetrics() {
        this(Collections.emptyList());
    }

    public NetworkMetrics(Iterable<Tag> tags) {
        this.networks = new SystemInfo().getHardware().getNetworkIFs();
        this.tags = tags;
    }

    @Override
    public void bindTo(MeterRegistry meterRegistry) {

        FunctionCounter.builder("system.network.bytes.received", "", value -> {
            networkStats.refresh();
            return networkStats.bytesReceived;
        }).tags(tags)
            .register(meterRegistry);

        FunctionCounter.builder("system.network.bytes.sent", "", value -> {
            networkStats.refresh();
            return networkStats.bytesSent;
        }).tags(tags)
            .register(meterRegistry);

        FunctionCounter.builder("system.network.packets.received", "", value -> {
            networkStats.refresh();
            return networkStats.packetsReceived;
        }).tags(tags)
            .register(meterRegistry);

        FunctionCounter.builder("system.network.packets.sent", "", value -> {
            networkStats.refresh();
            return networkStats.packetsReceived;
        }).tags(tags)
            .register(meterRegistry);
    }

    private class CachedNetworkStats {

        long lastRefresh = 0;

        public volatile long bytesReceived;
        public volatile long bytesSent;
        public volatile long packetsReceived;
        public volatile long packetsSent;

        public void refresh() {
            if (System.currentTimeMillis() - lastRefresh < 5_000) {
                return;
            }

            long bytesReceived = 0;
            long bytesSent = 0;
            long packetsReceived = 0;
            long packetsSent = 0;

            for (NetworkIF nif : networks) {
                nif.updateNetworkStats();
                bytesReceived += nif.getBytesRecv();
                bytesSent += nif.getBytesSent();
                packetsReceived += nif.getPacketsRecv();
                packetsSent += nif.getPacketsSent();

            }
            this.bytesReceived = bytesReceived;
            this.bytesSent = bytesSent;
            this.packetsReceived = packetsReceived;
            this.packetsSent = packetsSent;
            System.out.println(FormatUtil.formatBytes(bytesReceived));
        }
    }
}
