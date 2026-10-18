package ds454.locomotive.steamservice.steamapi;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ds454.locomotive.generated.grpc.Problem;
import ds454.locomotive.generated.grpc.ProblemType;
import ds454.locomotive.steamservice.config.Config;
import ds454.locomotive.steamservice.steamapi.GetOwnedGamesResponse.GameId;
import ds454.locomotive.steamservice.util.HttpHelper;
import ds454.locomotive.steamservice.util.Result;

public class SteamApiClient {

    private static final Logger log = LoggerFactory.getLogger(SteamApiClient.class);

    private static final String GetOwnedGameIdsPath = "/IPlayerService/GetOwnedGames/v0001/";

    private final String token;
    private final String host;
    private final String format;

    public SteamApiClient(Config config) {
        this.token = config.webApiToken;
        this.host = config.webApiHost;
        this.format = config.webApiFormat;
    }

    public Result<List<Integer>, Problem> getOwnedGameIds(String steamId) {
        Map<String, String> pathParams = new HashMap<String, String>(3);
        pathParams.put("format", format);
        pathParams.put("key", token);
        pathParams.put("steamid", steamId);

        URI uri;
        switch (HttpHelper.uriBuilder(host + GetOwnedGameIdsPath, pathParams)) {
            case Result.Success(URI value) -> {
                uri = value;
            }
            case Result.Failure(Problem error) -> {
                return new Result.Failure<>(error);
            }
        }

        HttpRequest r = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response;
        try {
            response = HttpHelper.client.send(r, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            log.error("Problem connecting to Steam API: {}", e.getClass().toString());
            return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.UnableToConnectToSteam).build());
        }

        if (response.statusCode() == 401) {
            log.error("Steam WebAPI key is invalid");
            return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.InvalidSteamWebApiToken).build());
        }
        if (response.statusCode() != 200) {
            log.error("Received response code {} from Steam API", response.statusCode());
            return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.UnableToConnectToSteam).build());
        }

        String responseBody = response.body();
        ObjectMapper m = HttpHelper.mapper;
        GetOwnedGamesResponse ownedGames;
        try {
            ownedGames = m.readValue(responseBody, GetOwnedGamesResponse.class);
        } catch (JsonProcessingException e) {
            return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
        }

        List<Integer> gameIds = new ArrayList<>(ownedGames.response().games().size());
        for (GameId gameId : ownedGames.response().games())
            gameIds.add((int) gameId.appid());

        return new Result.Success<>(gameIds);
    }
}
