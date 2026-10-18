package ds454.locomotive.steamservice.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);
    
    private static final String webApiTokenName = "STEAM_WEBAPI_TOKEN";
    private static final String webApiHostName = "STEAM_WEBAPI_HOST";
    private static final String webApiFormatName = "STEAM_WEBAPI_FORMAT";
    private static final String portName = "GRPC_PORT";

    public final String webApiToken;
    public final String webApiHost;
    public final String webApiFormat;
    public final Integer port;

    public Config() {
        Map<String, String> env = System.getenv();

        if (!env.containsKey(webApiTokenName)) {
            log.error("Missing required env variable '{}'", webApiTokenName);
            throw new Error();
        }
        this.webApiToken = env.get(webApiTokenName);

        if (!env.containsKey(webApiHostName)) {
            log.error("Missing required env variable '{}'", webApiHostName);
            throw new Error();
        }
        this.webApiHost = env.get(webApiHostName);

        if (!env.containsKey(webApiFormatName)) {
            log.error("Missing required env variable '{}'", webApiFormatName);
            throw new Error();
        }
        this.webApiFormat = env.get(webApiFormatName);

        if (!env.containsKey(portName)) {
            log.error("Missing required env variable '{}'", portName);
            throw new Error();
        }
        this.port = Integer.parseInt(env.get(portName));
    }
}
