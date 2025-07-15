package requests;
import com.google.gson.Gson;

import java.util.Map;

public record LoginRequest(String username, String password) {
}
