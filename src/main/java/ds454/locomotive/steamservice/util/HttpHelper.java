package ds454.locomotive.steamservice.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import ds454.locomotive.generated.grpc.Problem;
import ds454.locomotive.generated.grpc.ProblemType;

public class HttpHelper {
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
}
