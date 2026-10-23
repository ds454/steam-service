package ds454.locomotive.steamservice.middleware;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ds454.locomotive.generated.grpc.GameFromSteam;
import ds454.locomotive.generated.grpc.GetGamesFromGameAppIdsRequest;
import ds454.locomotive.generated.grpc.GetGamesFromGameAppIdsResponse;
import ds454.locomotive.generated.grpc.Problem;
import ds454.locomotive.steamservice.steamapi.SteamApiClient;
import ds454.locomotive.steamservice.steamapi.model.GameFromAppId;
import ds454.locomotive.steamservice.util.Result;

public class SteamApiMiddleware {

    private static final Logger log = LoggerFactory.getLogger(SteamApiMiddleware.class);

    public static GetGamesFromGameAppIdsResponse getGamesFromGameAppIds(GetGamesFromGameAppIdsRequest request,
            SteamApiClient c) {
        List<Long> appIds = request.getAppIdsList();
        var result = GetGamesFromGameAppIdsResponse.newBuilder();

        List<GameFromAppId> games = new ArrayList<>(appIds.size());
        List<Long> problemGames = new ArrayList<>();

        for (Long appId : appIds) {
            log.info("Getting app id {}", appId);
            var game =  request.hasRateLimitMs() ? c.getGameFromAppId(appId, request.getRateLimitMs()) : c.getGameFromAppId(appId, 0);
            switch (game) {
                case Result.Success(GameFromAppId value) -> {
                    if (value.isValid())
                        games.add(value);
                    else {
                        log.warn("App ID {} failed", appId);
                        problemGames.add(appId);
                    }
                }
                case Result.Failure(Problem _) -> {
                    log.warn("App ID {} failed", appId);
                    problemGames.add(appId);
                }
            }
        }

        for (GameFromAppId game : games) {
            result.addGames(
                    GameFromSteam
                            .newBuilder()
                            .setAppId(game.data().steamAppId())
                            .setName(game.data().name())
                            .setDescription(game.data().shortDescription())
                            .setReleaseYear(game.data().releaseYear())
                            .setDeveloper(game.data().developer())
                            .build());
        }
        result.addAllFailedAppIds(problemGames);

        return result.build();
    }
}
