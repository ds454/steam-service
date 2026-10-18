package ds454.locomotive.steamservice.grpc;

import java.util.List;

import ds454.locomotive.generated.grpc.GameIdList;
import ds454.locomotive.generated.grpc.GetOwnedGameIdsRequest;
import ds454.locomotive.generated.grpc.GetOwnedGameIdsResponse;
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
    public void getOwnedGames(GetOwnedGameIdsRequest request, StreamObserver<GetOwnedGameIdsResponse> responseObserver) {
        var response = GetOwnedGameIdsResponse.newBuilder();
        switch (steamApiClient.getOwnedGameIds(request.getSteamId())) {
			case Result.Success(List<Integer> value) -> {
                response.setGameIds(GameIdList.newBuilder().addAllGameIds(value));
            }
			case Result.Failure(Problem error) -> {
                response.setProblem(error);
            }
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
