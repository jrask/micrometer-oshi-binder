package se.flapsdown.micrometer.oshi.system;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

import java.util.Collections;

public class NetworkMetrics implements MeterBinder {

    private final Iterable<Tag> tags;
    private final NetworkIF[] networks;

    public NetworkMetrics() {
        this(Collections.emptyList());
    }

    public NetworkMetrics(Iterable<Tag> tags) {
        this.networks = new SystemInfo().getHardware().getNetworkIFs();
        this.tags = tags;
    }

    @Override
    public void bindTo(MeterRegistry meterRegistry) {


    }
}
