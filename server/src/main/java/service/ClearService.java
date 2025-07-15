package service;
import dataaccess.*;
import com.google.gson.Gson;
import jdk.jshell.spi.ExecutionControl;
import server.Server;

import java.util.Map;

public class ClearService {
    private GameDAO gameDAO;
    private UserDAO userDAO;
    private AuthDAO authDAO;
    public ClearService(GameDAO gameDAO, UserDAO userDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }


    public void clear() {
        throw new ExecutionControl.NotImplementedException("Not Implemented");
        res.type("application/json");
        return new Gson().toJson(Map.of("name", names));
    }
}
