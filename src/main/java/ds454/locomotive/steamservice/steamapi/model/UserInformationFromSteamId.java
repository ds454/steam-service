package ds454.locomotive.steamservice.steamapi.model;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ds454.locomotive.generated.grpc.Problem;
import ds454.locomotive.generated.grpc.ProblemType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserInformationFromSteamId(Response response) {

    public Optional<Problem> getProblems() {
        if (response().players() == null || response().players().isEmpty()) return Optional.of(Problem.newBuilder().setProblem(ProblemType.InvalidSteamID).build());
        if (response().players().size() != 1) return Optional.of(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
        var p = response.players().getFirst();

        if (p.visibilityState() == null || p.visibilityState() != 3) return Optional.of(Problem.newBuilder().setProblem(ProblemType.SteamAccountIsPrivate).build());

        if (p.username() == null || p.username().isBlank()) return Optional.of(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
        if (p.name() == null || p.name().isBlank()) return Optional.of(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
        if (p.lastSeen() == null) return Optional.of(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
        if (p.profileState() == null) return Optional.of(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
        if (p.personaState() == null) return Optional.of(Problem.newBuilder().setProblem(ProblemType.ServerError).build());

        return Optional.empty();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(List<Player> players) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Player(
            @JsonProperty("personaname") String username,
            @JsonProperty("realname") String name,
            @JsonProperty("lastlogoff") Long lastSeen,
            @JsonProperty("profilestate") Integer profileState,
            @JsonProperty("personastate") Integer personaState,
            @JsonProperty("communityvisibilitystate") Integer visibilityState) {
    }
}
