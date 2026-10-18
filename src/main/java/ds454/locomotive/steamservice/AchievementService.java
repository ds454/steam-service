package ds454.locomotive.steamservice;

import ds454.locomotive.generated.grpc.AcheivementsGrpc.AcheivementsImplBase;
import ds454.locomotive.generated.grpc.GetAllGameAcheivementsRequest;
import ds454.locomotive.generated.grpc.GetAllGameAcheivementsResponse;
import ds454.locomotive.generated.grpc.GetUserGameAcheivementsRequest;
import ds454.locomotive.generated.grpc.GetUserGameAcheivementsResponse;
import ds454.locomotive.generated.grpc.Problem;
import ds454.locomotive.generated.grpc.ProblemType;
import io.grpc.stub.StreamObserver;

public class AchievementService extends AcheivementsImplBase {

    @Override
    public void getAllGameAcheivements(
            GetAllGameAcheivementsRequest request,
            StreamObserver<GetAllGameAcheivementsResponse> responseObserver) {
        responseObserver.onNext(GetAllGameAcheivementsResponse.newBuilder().build());
        Problem.newBuilder().setMessage("Problem connecting to steam's servers").setProblem(ProblemType.InvalidSteamID).build();
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