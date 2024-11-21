package config;

import lombok.extern.slf4j.Slf4j;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class ConnectionManager {

    private static final String url = PropertiesUtils.get("db.url");
    private static final String username = PropertiesUtils.get("db.username", "root");
    private static final String password = PropertiesUtils.get("db.password", "root");

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            log.error("Ошибка при регистрации драйвера PostgreSQL: ", e);
            throw new RuntimeException("Ошибка при регистрации драйвера PostgreSQL", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            log.debug("Создание соединения с базой данных...");
            Connection connection = DriverManager.getConnection(url, username, password);
            log.info("Соединение с базой данных установлено.");
            return connection;
        } catch (SQLException e) {
            log.error("Ошибка при подключении к базе данных: {}", e.getMessage(), e);
            throw e;
        }
    }

    public static void testConnection() throws SQLException {
        try (Connection connection = getConnection()) {
            if (connection.isValid(5)) {
                log.info("Тест соединения с базой данных успешно выполнен.");
            } else {
                throw new SQLException("Соединение с базой данных недействительно.");
            }
        }
    }
}
