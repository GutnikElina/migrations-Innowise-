package config;

import lombok.extern.slf4j.Slf4j;
import java.sql.Connection;
import java.sql.DriverManager;

@Slf4j
public class ConnectionManager {

    private final String url;
    private final String username;
    private final String password;

    public ConnectionManager(PropertiesUtils properties) {
        this.url = properties.get("db.url");
        this.username = properties.get("db.username", "root");
        this.password = properties.get("db.password");
        log.debug("Инициализация ConnectionManager с URL: {}", url);
    }

    public Connection getConnection() throws Exception {
        log.debug("Создание соединения с базой данных...");
        Connection connection = DriverManager.getConnection(url, username, password);
        log.info("Соединение с базой данных установлено.");
        return connection;
    }
}
