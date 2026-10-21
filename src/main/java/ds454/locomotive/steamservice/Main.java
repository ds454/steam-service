package ds454.locomotive.steamservice;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ds454.locomotive.steamservice.config.Config;
import ds454.locomotive.steamservice.grpc.SteamService;
import ds454.locomotive.steamservice.steamapi.SteamApiClient;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        log.info("Loading config from environment...");
        var config = new Config();
        log.info("Done");

        SteamApiClient.init(config);

        log.info("Starting gRPC server...");
        Server server = ServerBuilder.forPort(config.port)
                .addService(new SteamService(SteamApiClient.get()))
                .build()
                .start();
        log.info("gRPC server started.");
        log.info("Logging is enabled");

        // Handle clean shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server...");
            server.shutdown();
            try {
                if (!server.awaitTermination(10, TimeUnit.SECONDS)) {
                    server.shutdownNow();
                }
            } catch (InterruptedException e) {
                server.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));

        server.awaitTermination();
        log.info("Goodbye!");
    }
}
