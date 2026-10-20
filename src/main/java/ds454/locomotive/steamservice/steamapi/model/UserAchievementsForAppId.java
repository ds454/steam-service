package ds454.locomotive.steamservice.steamapi.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserAchievementsForAppId(PlayerStats playerstats) {

    public boolean clean() {
        if (playerstats() == null)
            return false;
        return playerstats.clean();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerStats(List<Achievement> achievements) {

        public boolean clean() {
            if (achievements() == null || achievements().isEmpty())
                return false;
            achievements().removeIf(a -> a == null || !a.isValid());
            if (achievements().size() == 0)
                return false;
            return true;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Achievement(
            @JsonProperty("apiname") String name,
            Boolean achieved,
            @JsonProperty("unlocktime") Long unlockTime) {

        public boolean isValid() {
            if (name() == null || name().isBlank())
                return false;
            if (achieved() == null)
                return false;
            if (unlockTime() == null)
                return false;
            return true;
        }
    }
}