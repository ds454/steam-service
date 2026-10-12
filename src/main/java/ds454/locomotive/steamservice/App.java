package ds454.locomotive.steamservice;

import java.util.Map;

public class App {
    public static void main(String[] args) {
        Map<String, String> variables = System.getenv();
        var name = variables.get("NAME") == null ? "I am nameless" : "My name is " + variables.get("NAME");
        var hasSteamKey = variables.containsKey("STEAM_API_KEY") ? "I have a steam key :)" : "I don't have a steam key :(";
        System.out.printf("Hello! %s, and %s\n", name, hasSteamKey);
    }
}
