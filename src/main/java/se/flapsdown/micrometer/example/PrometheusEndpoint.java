package se.flapsdown.micrometer.example;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import se.flapsdown.micrometer.oshi.system.SystemMetricsSetup;
import se.flapsdown.micrometer.oshi.system.TelegrafProcessorMetrics;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class PrometheusEndpoint {

    public static void main(String args[]) {

        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        SystemMetricsSetup.builder(SystemMetricsSetup.Flavour.TELEGRAF)
            .withProcessorMetrics()
            .withMemoryMetrics()
            .bindTo(prometheusRegistry);

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
