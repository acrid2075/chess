package response;

import model.GameData;

import java.util.Collection;

public record BlanketResponse(Collection<GameData> games, int gameID, String username, String authToken, String message) {
}
