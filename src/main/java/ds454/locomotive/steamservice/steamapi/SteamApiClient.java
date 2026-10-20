package ds454.locomotive.steamservice.steamapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ds454.locomotive.generated.grpc.Problem;
import ds454.locomotive.generated.grpc.ProblemType;
import ds454.locomotive.steamservice.config.Config;
import ds454.locomotive.steamservice.steamapi.model.AchievementsForAppId;
import ds454.locomotive.steamservice.steamapi.model.FriendList;
import ds454.locomotive.steamservice.steamapi.model.GameFromAppId;
import ds454.locomotive.steamservice.steamapi.model.GetOwnedGames;
import ds454.locomotive.steamservice.steamapi.model.GetOwnedGames.GameId;
import ds454.locomotive.steamservice.steamapi.model.UserAchievementsForAppId;
import ds454.locomotive.steamservice.steamapi.model.UserInformationFromSteamId;
import ds454.locomotive.steamservice.util.HttpHelper;
import ds454.locomotive.steamservice.util.Result;

public class SteamApiClient {

    private static final Logger log = LoggerFactory.getLogger(SteamApiClient.class);

    private static final String GetOwnedGameIdsPath = "/IPlayerService/GetOwnedGames/v0001/";
    private static final String GetGameFromAppId = "/appdetails/";
    private static final String GetAchievementsForAppId = "/ISteamUserStats/GetSchemaForGame/v2/";
    private static final String GetUserAchievementsForAppId = "/ISteamUserStats/GetPlayerAchievements/v0001/";
    private static final String GetUserInformationFromSteamId = "/ISteamUser/GetPlayerSummaries/v0002/";
    private static final String GetFriendList = "/ISteamUser/GetFriendList/v1/";

    private final String token;
    private final String host;
    private final String format;

    private final String storeHost;
    private final String storeHostContext;

    public SteamApiClient(Config config) {
        this.token = config.webApiToken;
        this.host = config.webApiHost;
        this.format = config.webApiFormat;

        this.storeHost = config.storeApiHost;
        this.storeHostContext = config.storeApiContext;
    }

    public Result<List<Integer>, Problem> getOwnedGameIds(String steamId) {
        Map<String, String> pathParams = new HashMap<String, String>(3);
        pathParams.put("format", format);
        pathParams.put("key", token);
        pathParams.put("steamid", steamId);

        GetOwnedGames ownedGames;
        switch (HttpHelper.<GetOwnedGames>get(host + GetOwnedGameIdsPath, pathParams,
                GetOwnedGames.class)) {
            case Result.Success(GetOwnedGames value) -> ownedGames = value;
            case Result.Failure(Problem error) -> {
                return new Result.Failure<>(error);
            }
        }

        List<Integer> gameIds = new ArrayList<>(ownedGames.response().games().size());
        for (GameId gameId : ownedGames.response().games())
            gameIds.add((int) gameId.appid());

        return new Result.Success<>(gameIds);
    }

    public Result<GameFromAppId, Problem> getGameFromAppId(int appId) {
        Map<String, String> pathParams = new HashMap<String, String>(2);
        pathParams.put("format", format);
        pathParams.put("appids", Integer.toString(appId));

        GameFromAppId r;
        switch (HttpHelper.<GameFromAppId>get(storeHost + storeHostContext + GetGameFromAppId, pathParams,
                GameFromAppId.class)) {
            case Result.Success(GameFromAppId value) -> r = value;
            case Result.Failure(Problem error) -> {
                return new Result.Failure<>(error);
            }
        }

        if (r.isValid())
            return new Result.Success<>(r);

        return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
    }

    public Result<AchievementsForAppId, Problem> getAchievementsForAppId(int appId) {
        Map<String, String> pathParams = new HashMap<String, String>(2);
        pathParams.put("format", format);
        pathParams.put("key", token);
        pathParams.put("l", "english");
        pathParams.put("appids", Integer.toString(appId));

        AchievementsForAppId a;
        switch (HttpHelper.<AchievementsForAppId>get(host + GetAchievementsForAppId, pathParams,
                AchievementsForAppId.class)) {
            case Result.Success(AchievementsForAppId value) -> a = value;
            case Result.Failure(Problem error) -> {
                return new Result.Failure<>(error);
            }
        }

        return new Result.Success<>(a);
    }

    public Result<UserAchievementsForAppId, Problem> getUserAchievementsForAppId(int appId, String steamId) {
        Map<String, String> pathParams = new HashMap<String, String>(2);
        pathParams.put("format", format);
        pathParams.put("key", token);
        pathParams.put("steamid", steamId);
        pathParams.put("appid", Integer.toString(appId));

        UserAchievementsForAppId a;
        switch (HttpHelper.<UserAchievementsForAppId>get(host + GetUserAchievementsForAppId, pathParams,
                UserAchievementsForAppId.class)) {
            case Result.Success(UserAchievementsForAppId value) -> {
                a = value;
            }
            case Result.Failure(Problem error) -> {
                return new Result.Failure<>(error);
            }
        }

        if (a.clean())
            return new Result.Success<>(a);

        return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
    }

    public Result<UserInformationFromSteamId, Problem> getUserInformationFromSteamId(String steamId) {
        Map<String, String> pathParams = new HashMap<String, String>(2);
        pathParams.put("format", format);
        pathParams.put("key", token);
        pathParams.put("steamids", steamId);

        UserInformationFromSteamId u;
        switch (HttpHelper.<UserInformationFromSteamId>get(host + GetUserInformationFromSteamId, pathParams,
                UserInformationFromSteamId.class)) {
            case Result.Success(UserInformationFromSteamId value) ->
                u = value;
            case Result.Failure(Problem error) -> {
                return new Result.Failure<>(error);
            }
        }

        var p = u.getProblems();
        if (p.isEmpty())
            return new Result.Success<>(u);

        return new Result.Failure<>(p.get());
    }
    
    public Result<FriendList, Problem> getFriendListFromSteamId(String steamId) {
        Map<String, String> pathParams = new HashMap<String, String>(2);
        pathParams.put("key", token);
        pathParams.put("steamids", steamId);
        pathParams.put("relationship", "friend");

        FriendList f;
        switch (HttpHelper.<FriendList>get(host + GetFriendList, pathParams,
                FriendList.class)) {
            case Result.Success(FriendList value) ->
                f = value;
            case Result.Failure(Problem error) -> {
                return new Result.Failure<>(error);
            }
        }

        if (!f.isValid()) {
            return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.FriendsListIsPrivate).build());
        }
        return new Result.Success<>(f);
    }

}
