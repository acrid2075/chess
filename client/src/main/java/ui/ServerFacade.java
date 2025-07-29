package ui;

import com.google.gson.Gson;
import response.BlanketResponse;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class ServerFacade {
    private final String serverURL;
    private String authToken = null;

    public ServerFacade(String url) {serverURL = url;}

    public void clear() {
        var path = "/db";
        Map request = Map.of();
        this.makeRequest("DELETE", path, request, BlanketResponse.class);
    }

    public BlanketResponse register(String username, String password, String email) {
        var path = "/user";
        Map request = Map.of("username", username, "password", password, "email", email);
        BlanketResponse out = this.makeRequest("POST", path, request, BlanketResponse.class);
        this.authToken = out.authToken();
        return out;
    }

    public BlanketResponse login(String username, String password) {
        var path = "/session";
        Map request = Map.of("username", username, "password", password);
        BlanketResponse out = this.makeRequest("POST", path, request, BlanketResponse.class);
        this.authToken = out.authToken();
        return out;
    }

    public void logout() {
        var path = "/session";
        Map request = Map.of();
        this.makeRequest("DELETE", path, request, null);
        this.authToken = null;
    }

    public BlanketResponse listGames() {
        var path = "/game";
        Map request = Map.of();
        return this.makeRequest("GET", path, request, BlanketResponse.class);
    }

    public BlanketResponse createGame(String gameName) {
        var path = "/game";
        Map request = Map.of("gameName", gameName);
        return this.makeRequest("POST", path, request, BlanketResponse.class);
    }

    public void joinGame(String playerColor, int gameID) {
        var path = "/game";
        Map request = Map.of("playerColor", playerColor, "gameID", gameID);
        this.makeRequest("PUT", path, request, null);
    }


    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) {
        try {
            URL url = (new URI(serverURL + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.addRequestProperty("Authorization", this.authToken);
            if (!method.equals("GET")) {
                http.setDoOutput(true);
                writeBody(request, http);
            }
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status + new Gson().fromJson(
                    new InputStreamReader(http.getErrorStream()), BlanketResponse.class));
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            var status = http.getResponseCode();
            if (status >= 200 && status < 300) {
                try (InputStream respBody = http.getInputStream()) {
                    InputStreamReader reader = new InputStreamReader(respBody);
                    if (responseClass != null) {
                        response = new Gson().fromJson(reader, responseClass);
                    }
                }
            } else {
                try (InputStream respBody = http.getErrorStream()) {
                    InputStreamReader reader = new InputStreamReader(respBody);
                    if (responseClass != null) {
                        response = new Gson().fromJson(reader, responseClass);
                    }
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) {return status / 100 == 2;}
}
