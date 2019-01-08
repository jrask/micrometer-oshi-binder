package se.flapsdown.micrometer.example;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepRegistryConfig;
import se.flapsdown.micrometer.oshi.system.MemoryMetrics;
import se.flapsdown.micrometer.oshi.system.NetworkMetrics;
import se.flapsdown.micrometer.oshi.system.ProcessorMetrics;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PrometheusEndpoint {

    public static void main(String args[]) {


        final SimpleMeterRegistry simpleMeterRegistry = new SimpleMeterRegistry();

        StepRegistryConfig conf = new StepRegistryConfig() {
            @Override
            public String prefix() {
                return null;
            }

            @Override
            public String get(String s) {
                return null;
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(5);
            }

        };

        StepMeterRegistry reg = new StepMeterRegistry(conf, Clock.SYSTEM) {

            protected void publish() {

                System.out.println("#publish");
                List<Meter> meters = getMeters();
                for (Meter m : meters) {
                    Meter stepMeter = simpleMeterRegistry.get(m.getId().getName()).meter();

                    if (m instanceof FunctionCounter) {
                        System.out.println(
                            name(m.getId().getName()) + ", step=" + ((FunctionCounter) m).count() +  ", simple=" +
                            ((FunctionCounter) stepMeter).count() + ", " + getConventionTags(m.getId())
                            );

                    }

                    if (m instanceof Gauge) {
                        System.out.println(
                            name(m.getId().getName()) + ", step=" + ((Gauge) m).value() +  ", simple=" +
                                ((Gauge) stepMeter).value() + ", " + getConventionTags(m.getId())
                        );
                    }
                }

            }

            @Override
            protected TimeUnit getBaseTimeUnit() {
                return TimeUnit.MILLISECONDS;
            }
        };

        CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry(Clock.SYSTEM);
        compositeMeterRegistry.add(reg);

        compositeMeterRegistry.add(simpleMeterRegistry);
        new NetworkMetrics().bindTo(compositeMeterRegistry);
        new MemoryMetrics().bindTo(compositeMeterRegistry);
        new ProcessorMetrics().bindTo(compositeMeterRegistry);
        reg.start();

    }

    public static String name(String string) {
        return String.format("%1$"+31+ "s", string);
    }

}
