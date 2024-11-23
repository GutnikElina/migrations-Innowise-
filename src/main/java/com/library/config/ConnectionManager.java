package com.library.config;

import lombok.extern.slf4j.Slf4j;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Класс для управления соединениями с базой данных.
 * Он предоставляет методы для получения соединения с базой данных и тестирования соединения
 */
@Slf4j
public class ConnectionManager {

    private static final String url = PropertiesUtils.get("db.url");
    private static final String username = PropertiesUtils.get("db.username", "root");
    private static final String password = PropertiesUtils.get("db.password", "root");

    static {
        try {
            // Регистрируем драйвер вручную (несмотря на то,
            // что есть зависимость в pom.xml) для совместимости с более
            // старыми версиями Java или специфическими контейнерами
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            log.error("Driver PostgreSQL not found: ", e);
            throw new RuntimeException("Error registering PostgreSQL driver", e);
        }
    }

    /**
     * Получает соединение с базой данных.
     *
     * @return объект {@link Connection} для взаимодействия с базой данных.
     * @throws SQLException если не удалось установить соединение с базой данных.
     */
    public static Connection getConnection() throws SQLException {
        try {
            log.debug("Connecting to the database...");
            Connection connection = DriverManager.getConnection(url, username, password);
            if (connection == null || !connection.isValid(5)) {
                throw new SQLException("Invalid connection URL: " + url);
            }
            log.info("Database connection established.");
            return connection;
        } catch (SQLException e) {
            log.error("Connection error: URL={}; user={}", url, username, e);
            throw e;
        }
    }


    /**
     * Тестирует соединение с базой данных.
     * Попытка установить соединение с базой и проверить его валидность.
     *
     * @throws SQLException если соединение не удалось установить или оно невалидно.
     */
    public static void testConnection() throws SQLException {
        try (Connection connection = getConnection()) {
            if (connection.isValid(5)) {
                log.info("Database connection test passed.");
            } else {
                throw new SQLException("Database connection wasn't successful.");
            }
        }
    }
}
