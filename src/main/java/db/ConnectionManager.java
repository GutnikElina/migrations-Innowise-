package db;

import config.PropertiesUtils;
import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionManager {

    private final String url;
    private final String username;
    private final String password;

    public ConnectionManager(PropertiesUtils properties) {
        this.url = properties.get("db.url");
        this.username = properties.get("db.username", "root");
        this.password = properties.get("db.password");
    }

    public Connection getConnection() throws Exception {
        return DriverManager.getConnection(url, username, password);
    }
}