package ds454.locomotive.steamservice.steamapi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GetOwnedGamesResponse(Response response) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(List<GameId> games) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GameId(long appid) {
    }
}
