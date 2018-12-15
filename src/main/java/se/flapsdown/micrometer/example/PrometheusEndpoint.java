package se.flapsdown.micrometer.example;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepRegistryConfig;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import se.flapsdown.micrometer.oshi.system.NetworkMetrics;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PrometheusEndpoint {

    public static void main(String args[]) {


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

            @Override
            public boolean enabled() {
                return true;
            }
        };

        StepMeterRegistry reg = new StepMeterRegistry(conf, Clock.SYSTEM) {
            @Override
            protected void publish() {
                System.out.println("=> publish()");
                List<Meter> meters = getMeters();
                for (Meter m : meters) {
                    if (m instanceof FunctionCounter) {
                        System.out.println(getConventionName(m.getId()) + " : " + ((FunctionCounter)m).count());
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
        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        compositeMeterRegistry.add(prometheusRegistry);
        new NetworkMetrics().bindTo(compositeMeterRegistry);
        reg.start();

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(9092), 0);
            server.createContext("/metrics", httpExchange -> {
                System.out.println("scrape()");
                String response = prometheusRegistry.scrape();
                System.out.println(response);
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });

            new Thread(server::start).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
