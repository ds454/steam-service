package ds454.locomotive.steamservice;

import ds454.locomotive.generated.grpc.AcheivementsGrpc.AcheivementsImplBase;
import ds454.locomotive.generated.grpc.GetAllGameAcheivementsRequest;
import ds454.locomotive.generated.grpc.GetAllGameAcheivementsResponse;
import ds454.locomotive.generated.grpc.GetUserGameAcheivementsRequest;
import ds454.locomotive.generated.grpc.GetUserGameAcheivementsResponse;
import io.grpc.stub.StreamObserver;

public class AchievementService extends AcheivementsImplBase {

    @Override
    public void getAllGameAcheivements(
            GetAllGameAcheivementsRequest request,
            StreamObserver<GetAllGameAcheivementsResponse> responseObserver) {
        responseObserver.onNext(GetAllGameAcheivementsResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUserGameAcheivements(
            GetUserGameAcheivementsRequest request,
            StreamObserver<GetUserGameAcheivementsResponse> responseObserver) {
        responseObserver.onNext(GetUserGameAcheivementsResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}