package ds454.locomotive.steamservice.steamapi.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FriendList(@JsonProperty("friendslist") FriendsList friendsList) {

    public boolean isValid() {
        if (friendsList() == null) return false;

        return true;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FriendsList(List<Friend> friends) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Friend(String steamid) {
    }
}