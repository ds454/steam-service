package ds454.locomotive.steamservice.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ds454.locomotive.generated.grpc.Problem;
import ds454.locomotive.generated.grpc.ProblemType;

public class HttpHelper {
    private static final Logger log = LoggerFactory.getLogger(HttpHelper.class);

    public static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    public static final ObjectMapper mapper = new ObjectMapper();

    private static final int backoffTimeMs = 30000;
    private static final int backoffsBeforeFailure = 5;

    public static Result<URI, Problem> uriBuilder(String endpoint, Map<String, String> pathParams) {
        if (pathParams == null || pathParams.isEmpty()) {
            try {
                return new Result.Success<>(new URI(endpoint));
            } catch (URISyntaxException e) {
                return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
            }
        }
        StringBuilder uri = new StringBuilder(endpoint);
        uri.append("?");
        for (String key : pathParams.keySet())
            uri.append(URLEncoder.encode(key, StandardCharsets.UTF_8) + "="
                    + URLEncoder.encode(pathParams.get(key), StandardCharsets.UTF_8) + "&");

        // Remove final '&'
        uri.deleteCharAt(uri.length() - 1);

        try {
            return new Result.Success<>(new URI(uri.toString()));
        } catch (URISyntaxException e) {
            return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
        }
    }

    public static <T> Result<T, Problem> get(String endpoint, Map<String, String> pathParams, Class<T> clazz) {
        URI uri;
        switch (HttpHelper.uriBuilder(endpoint, pathParams)) {
            case Result.Success(URI value) -> {
                uri = value;
            }
            case Result.Failure(Problem error) -> {
                return new Result.Failure<>(error);
            }
        }

        HttpRequest r = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<String> response;
        var failedAttempts = 0;
        do {
            if (failedAttempts >= backoffsBeforeFailure) {
                log.error("Too many failures, quitting");
                return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
            }
            if (failedAttempts > 0) {
                log.warn("Backing off before trying again (waiting {} ms)...", failedAttempts * backoffTimeMs);
                try {
                    Thread.sleep(failedAttempts * backoffTimeMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            try {
                response = HttpHelper.client.send(r, BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                log.error("Problem connecting to Steam API: {}", e.getClass().toString());
                return new Result.Failure<>(
                        Problem.newBuilder().setProblem(ProblemType.UnableToConnectToSteam).build());
            }

            if (response.statusCode() == 401) {
                failedAttempts++;
                log.warn("Auth Error");
            }
            else if (response.statusCode() == 429) {
                failedAttempts++;
                log.warn("Rate limited");
            }
            else if (response.statusCode() != 200) {
                log.error("Received response code {}", response.statusCode());
                return new Result.Failure<>(
                        Problem.newBuilder().setProblem(ProblemType.UnableToConnectToSteam).build());
            }
            else break;
        } while (true);

        ObjectMapper m = HttpHelper.mapper;
        try {
            return new Result.Success<>(m.readValue(response.body(), clazz));
        } catch (Exception e) {
            return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
        }
    }
}
