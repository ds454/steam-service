package ds454.locomotive.steamservice.grpc;

import java.util.List;

import ds454.locomotive.generated.grpc.AppIdList;
import ds454.locomotive.generated.grpc.GetGameFromGameAppIdRequest;
import ds454.locomotive.generated.grpc.GetGameFromGameAppIdResponse;
import ds454.locomotive.generated.grpc.GetGamesFromGameAppIdsRequest;
import ds454.locomotive.generated.grpc.GetGamesFromGameAppIdsResponse;
import ds454.locomotive.generated.grpc.GetOwnedGameAppIdsRequest;
import ds454.locomotive.generated.grpc.GetOwnedGameAppIdsResponse;
import ds454.locomotive.generated.grpc.Problem;
import ds454.locomotive.generated.grpc.SteamServiceGrpc.SteamServiceImplBase;
import ds454.locomotive.steamservice.steamapi.SteamApiClient;
import ds454.locomotive.steamservice.util.Result;
import io.grpc.stub.StreamObserver;

public class SteamService extends SteamServiceImplBase {

    private final SteamApiClient steamApiClient;

    public SteamService(SteamApiClient steamApiClient) {
        this.steamApiClient = steamApiClient;
    }

    @Override
    public void getOwnedGameAppIds(GetOwnedGameAppIdsRequest request, StreamObserver<GetOwnedGameAppIdsResponse> responseObserver) {
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
    public void getGameFromGameAppId(GetGameFromGameAppIdRequest request, StreamObserver<GetGameFromGameAppIdResponse> responseObserver) {
        var response = GetGameFromGameAppIdResponse.newBuilder();
        
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getGamesFromGameAppIds(GetGamesFromGameAppIdsRequest request, StreamObserver<GetGamesFromGameAppIdsResponse> responseObserver) {
        var response = GetGamesFromGameAppIdsResponse.newBuilder();

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

}
