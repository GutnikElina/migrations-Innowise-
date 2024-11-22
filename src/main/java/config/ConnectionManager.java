package config;

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
            log.error("Драйвер PostgreSQL не найден: ", e);
            throw new RuntimeException("Ошибка при регистрации драйвера PostgreSQL", e);
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
            log.debug("Соединение с базой данных...");
            Connection connection = DriverManager.getConnection(url, username, password);
            log.info("Соединение с базой данных установлено.");
            return connection;
        } catch (SQLException e) {
            log.error("Ошибка подключения: URL={}; пользователь={} ", url, username, e);
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
                log.info("Тест соединения с базой данных успешно выполнен.");
            } else {
                throw new SQLException("Соединение с базой данных не выполняется успешно");
            }
        }
    }
}
