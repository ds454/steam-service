package ds454.locomotive.steamservice.grpc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ds454.locomotive.generated.grpc.AchievementFromSteam;
import ds454.locomotive.generated.grpc.AchievementsFromSteam;
import ds454.locomotive.generated.grpc.AppIdList;
import ds454.locomotive.generated.grpc.FriendSteamIds;
import ds454.locomotive.generated.grpc.GetAchievementsForGameAppIdRequest;
import ds454.locomotive.generated.grpc.GetAchievementsForGameAppIdResponse;
import ds454.locomotive.generated.grpc.GetFriendIdsFromSteamIdRequest;
import ds454.locomotive.generated.grpc.GetFriendIdsFromSteamIdResponse;
import ds454.locomotive.generated.grpc.GetGamesFromGameAppIdsRequest;
import ds454.locomotive.generated.grpc.GetGamesFromGameAppIdsResponse;
import ds454.locomotive.generated.grpc.GetOwnedGameAppIdsRequest;
import ds454.locomotive.generated.grpc.GetOwnedGameAppIdsResponse;
import ds454.locomotive.generated.grpc.GetUserAchievementsForGameAppIdRequest;
import ds454.locomotive.generated.grpc.GetUserAchievementsForGameAppIdResponse;
import ds454.locomotive.generated.grpc.GetUserInformationFromSteamIdRequest;
import ds454.locomotive.generated.grpc.GetUserInformationFromSteamIdResponse;
import ds454.locomotive.generated.grpc.Problem;
import ds454.locomotive.generated.grpc.SteamProfileState;
import ds454.locomotive.generated.grpc.SteamUserInformation;
import ds454.locomotive.generated.grpc.UserAchievementForGameAppId;
import ds454.locomotive.generated.grpc.UserAchievementsForGameAppId;
import ds454.locomotive.generated.grpc.SteamServiceGrpc.SteamServiceImplBase;
import ds454.locomotive.steamservice.middleware.SteamApiMiddleware;
import ds454.locomotive.steamservice.steamapi.SteamApiClient;
import ds454.locomotive.steamservice.steamapi.model.AchievementsForAppId;
import ds454.locomotive.steamservice.steamapi.model.FriendList;
import ds454.locomotive.steamservice.steamapi.model.UserAchievementsForAppId;
import ds454.locomotive.steamservice.steamapi.model.UserInformationFromSteamId;
import ds454.locomotive.steamservice.util.Result;
import io.grpc.stub.StreamObserver;

public class SteamService extends SteamServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(SteamService.class);

    private final SteamApiClient steamApiClient;

    public SteamService(SteamApiClient steamApiClient) {
        this.steamApiClient = steamApiClient;
    }

    @Override
    public void getOwnedGameAppIds(GetOwnedGameAppIdsRequest request,
            StreamObserver<GetOwnedGameAppIdsResponse> responseObserver) {
        log.debug("Received get owned games request");
        var response = GetOwnedGameAppIdsResponse.newBuilder();
        switch (steamApiClient.getOwnedGameIds(request.getSteamId())) {
            case Result.Success(List<Integer> value) -> {
                log.debug("Success");
                response.setAppIds(AppIdList.newBuilder().addAllAppIds(value));
            }
            case Result.Failure(Problem error) -> {
                log.debug("Failure");
                response.setProblem(error);
            }
        }
        log.debug("Finished get owned games request");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getGamesFromGameAppIds(GetGamesFromGameAppIdsRequest request,
            StreamObserver<GetGamesFromGameAppIdsResponse> responseObserver) {
        log.debug("Received get games from game app ids request");
        var response = SteamApiMiddleware.getGamesFromGameAppIds(request, steamApiClient);
        log.debug("Finished get games from game app ids request");
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUserInformationFromSteamId(
            GetUserInformationFromSteamIdRequest request,
            StreamObserver<GetUserInformationFromSteamIdResponse> responseObserver) {
        log.debug("Received get user info from steam id request");
        var response = GetUserInformationFromSteamIdResponse.newBuilder();

        var result = steamApiClient.getUserInformationFromSteamId(request.getSteamId());

        switch (result) {
            case Result.Success(UserInformationFromSteamId value) -> {
                log.debug("Success");
                SteamProfileState profileState;
                switch (value.response().players().getFirst().personaState()) {
                    case 0:
                        profileState = SteamProfileState.OFFLINE;
                    case 1:
                        profileState = SteamProfileState.ONLINE;
                    case 2:
                        profileState = SteamProfileState.BUSY;
                    case 3:
                        profileState = SteamProfileState.AWAY;
                    case 4:
                        profileState = SteamProfileState.SNOOZE;
                        // Looking to trade, looking to play, other
                    default:
                        profileState = SteamProfileState.ONLINE;
                }
                response.setSteamUserInfo(
                        SteamUserInformation
                                .newBuilder()
                                .setSteamUsername(value.response().players().getFirst().username())
                                .setProfileState(profileState)
                                .build());
            }
            case Result.Failure(Problem error) -> {
                log.debug("Failure");
                response.setProblem(error);
            }
        }

        log.debug("Finished get user info from steam id request");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getFriendsFromSteamId(GetFriendIdsFromSteamIdRequest request,
            StreamObserver<GetFriendIdsFromSteamIdResponse> responseObserver) {

        log.debug("Received get friends from steam id request");

        var response = GetFriendIdsFromSteamIdResponse.newBuilder();
        switch (steamApiClient.getFriendListFromSteamId(request.getSteamId())) {
            case Result.Success(FriendList value) -> {
                log.debug("Success");
                response.setFriendSteamIds(FriendSteamIds.newBuilder().addAllFriendSteamIds(
                        value.friendsList().friends().stream().map(friend -> friend.steamid()).toList()));
            }
            case Result.Failure(Problem error) -> {
                log.debug("Failure");
                response.setProblem(error);
            }
        }

        log.debug("Finished get friends from steam id request");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAchievementsForGameAppId(GetAchievementsForGameAppIdRequest request,
            StreamObserver<GetAchievementsForGameAppIdResponse> responseObserver) {

        log.debug("Received get achievements for game app id request");

        var response = GetAchievementsForGameAppIdResponse.newBuilder();
        switch (steamApiClient.getAchievementsForAppId(request.getAppId())) {
            case Result.Success(AchievementsForAppId value) -> {
                log.debug("Success");
                response.setAchievments(AchievementsFromSteam.newBuilder().addAllAchievementFromSteam(value.game()
                        .availableGameStats().achievements().stream()
                        .map((achievement) -> AchievementFromSteam.newBuilder().setAchievementName(achievement.name())
                                .setAchievementDisplayName(achievement.displayName())
                                .setDescription(achievement.description()).build())
                        .toList()));
            }
            case Result.Failure(Problem error) -> {
                log.debug("Failure");
                response.setProblem(error);
            }
        }

        log.debug("Finished get achievements for game app id request");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUserAchievementsForGameAppId(GetUserAchievementsForGameAppIdRequest request,
            StreamObserver<GetUserAchievementsForGameAppIdResponse> responseObserver) {
        log.debug("Received get user achievements for game app id request");

        var response = GetUserAchievementsForGameAppIdResponse.newBuilder();
        switch (steamApiClient.getUserAchievementsForAppId(request.getAppId(), request.getSteamId())) {
            case Result.Success(UserAchievementsForAppId value) -> {
                log.debug("Success");
                response.setAchievements(UserAchievementsForGameAppId.newBuilder().addAllAchievment(value.playerstats()
                        .achievements().stream()
                        .map(((achievement) -> UserAchievementForGameAppId.newBuilder()
                                .setAchievementName(achievement.name()).setCompleted(achievement.achieved())
                                .setTimeCompleted(achievement.achieved() ? achievement.unlockTime() : 0).build()))
                        .toList()));
            }
            case Result.Failure(Problem error) -> {
                log.debug("Failure");
                response.setProblem(error);
            }
        }

        log.debug("Finished get user achievements for game app id request");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
