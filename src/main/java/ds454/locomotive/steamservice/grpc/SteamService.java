package ds454.locomotive.steamservice.grpc;

import java.util.List;

import ds454.locomotive.generated.grpc.AppIdList;
import ds454.locomotive.generated.grpc.GetFriendIdsFromSteamIdRequest;
import ds454.locomotive.generated.grpc.GetFriendIdsFromSteamIdResponse;
import ds454.locomotive.generated.grpc.GetGamesFromGameAppIdsRequest;
import ds454.locomotive.generated.grpc.GetGamesFromGameAppIdsResponse;
import ds454.locomotive.generated.grpc.GetOwnedGameAppIdsRequest;
import ds454.locomotive.generated.grpc.GetOwnedGameAppIdsResponse;
import ds454.locomotive.generated.grpc.GetUserInformationFromSteamIdRequest;
import ds454.locomotive.generated.grpc.GetUserInformationFromSteamIdResponse;
import ds454.locomotive.generated.grpc.Problem;
import ds454.locomotive.generated.grpc.SteamProfileState;
import ds454.locomotive.generated.grpc.SteamUserInformation;
import ds454.locomotive.generated.grpc.SteamServiceGrpc.SteamServiceImplBase;
import ds454.locomotive.steamservice.middleware.SteamApiMiddleware;
import ds454.locomotive.steamservice.steamapi.SteamApiClient;
import ds454.locomotive.steamservice.steamapi.model.UserInformationFromSteamId;
import ds454.locomotive.steamservice.util.Result;
import io.grpc.stub.StreamObserver;

public class SteamService extends SteamServiceImplBase {

    private final SteamApiClient steamApiClient;

    public SteamService(SteamApiClient steamApiClient) {
        this.steamApiClient = steamApiClient;
    }

    @Override
    public void getOwnedGameAppIds(GetOwnedGameAppIdsRequest request,
            StreamObserver<GetOwnedGameAppIdsResponse> responseObserver) {
        var response = GetOwnedGameAppIdsResponse.newBuilder();
        switch (steamApiClient.getOwnedGameIds(request.getSteamId())) {
            case Result.Success(List<Integer> value) -> {
                response.setAppIds(AppIdList.newBuilder().addAllAppIds(value));
            }
            case Result.Failure(Problem error) -> {
                response.setProblem(error);
            }
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getGamesFromGameAppIds(GetGamesFromGameAppIdsRequest request,
            StreamObserver<GetGamesFromGameAppIdsResponse> responseObserver) {
        var response = SteamApiMiddleware.getGamesFromGameAppIds(request, steamApiClient);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUserInformationFromSteamId(
            GetUserInformationFromSteamIdRequest request,
            StreamObserver<GetUserInformationFromSteamIdResponse> responseObserver) {
        var response = GetUserInformationFromSteamIdResponse.newBuilder();

        var result = steamApiClient.getUserInformationFromSteamId(request.getSteamId());

        switch (result) {
            case Result.Success(UserInformationFromSteamId value) -> {
                SteamProfileState profileState;
                switch (value.response().players().getFirst().personaState()) {
                    case 0: profileState = SteamProfileState.OFFLINE;
                    case 1: profileState = SteamProfileState.ONLINE;
                    case 2: profileState = SteamProfileState.BUSY;
                    case 3: profileState = SteamProfileState.AWAY;
                    case 4: profileState = SteamProfileState.SNOOZE;
                    // Looking to trade, looking to play
                    default: profileState = SteamProfileState.ONLINE;
                }
                response.setSteamUserInfo(
                    SteamUserInformation
                        .newBuilder()
                        .setSteamUsername(value.response().players().getFirst().username())
                        .setProfileState(profileState)
                        .build()
                );
            }
            case Result.Failure(Problem error) -> response.setProblem(error);
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getFriendsFromSteamId(GetFriendIdsFromSteamIdRequest request,
            StreamObserver<GetFriendIdsFromSteamIdResponse> responseObserver) {
    }

}
