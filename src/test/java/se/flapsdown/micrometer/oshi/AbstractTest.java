package se.flapsdown.micrometer.oshi;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepRegistryConfig;
import io.micrometer.core.instrument.util.MeterPartition;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.flapsdown.micrometer.oshi.system.TelegrafProcessorMetrics;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 *
 * No tests yet, only for lab..
 *
 */
@Ignore
public class AbstractTest {


        StepRegistryConfig stepRegistryConfig = new StepRegistryConfig() {
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

        StepMeterRegistry m = new StepMeterRegistry(stepRegistryConfig, Clock.SYSTEM) {

            @Override
            public void publish() {
                System.out.println(" > publish()");
                for (List<Meter> batch : MeterPartition.partition(this, 1)) {

                    batch.stream()

                            .forEach(this::write);

                }

            }

            private void write(Meter meter) {
                if (meter instanceof Gauge) {
                    Gauge g = (Gauge)meter;
                    System.out.println(getConventionName(g.getId()) + " - " + g.value() / 1000000 );
                }
            }


            @Override
            protected TimeUnit getBaseTimeUnit() {
                return TimeUnit.MILLISECONDS;
            }
        };


        @Before
        public void prepare() throws InterruptedException {
            m.start();

        }


        @Test
        public void run() throws InterruptedException {
            new TelegrafProcessorMetrics().bindTo(m);
            Thread.sleep(300000);
        }
}
