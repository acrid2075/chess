package requests;

import com.google.gson.Gson;
import spark.*;

public record JoinGameRequest(int gameID, String username, String PlayerColor) {
}
