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
import ds454.locomotive.steamservice.steamapi.model.AchievementsForAppId;

public class HttpHelper {
    private static final Logger log = LoggerFactory.getLogger(HttpHelper.class);

    public static final HttpClient client = HttpClient.newHttpClient();
    public static final ObjectMapper mapper = new ObjectMapper();

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

        // Remove final ','
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
        try {
            response = HttpHelper.client.send(r, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            log.error("Problem connecting to Steam API: {}", e.getClass().toString());
            return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.UnableToConnectToSteam).build());
        }

        if (response.statusCode() == 401) {
            log.error("Authorization Error");
            return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.InvalidSteamWebApiToken).build());
        }
        if (response.statusCode() != 200) {
            log.error("Received response code {}", response.statusCode());
            return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.UnableToConnectToSteam).build());
        }

        ObjectMapper m = HttpHelper.mapper;
        try {
            return new Result.Success<>(m.readValue(response.body(), clazz));
        } catch (Exception e) {
            return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
        }
    }
}
