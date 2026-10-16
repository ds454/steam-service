package ds454.locomotive.steamservice;

import java.io.IOException;
import java.util.Map;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class Main {
    public static void main(String[] args) {
        Map<String, String> variables = System.getenv();
        var name = variables.get("NAME") == null ? "I am nameless" : "My name is " + variables.get("NAME");
        var hasSteamKey = variables.containsKey("STEAM_API_KEY") ? "I have a steam key :)" : "I don't have a steam key :(";
        System.out.printf("Hello! %s, and %s\n", name, hasSteamKey);

        Server s;

        try {
            s = ServerBuilder.forPort(80).addService(new AchievementService()).build().start();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Started gPRC Server!");

        try {
            s.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
