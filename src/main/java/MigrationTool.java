import config.ConnectionManager;
import lombok.extern.slf4j.Slf4j;
import migrations.MigrationManager;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Главный класс для выполнения миграций, который инициирует соединение с базой данных
 * и запускает процесс миграции
 */
@Slf4j
public class MigrationTool {

    /**
     * Точка входа для запуска миграций.
     * Осуществляет подключение к базе данных и запускает миграции
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        try {
            ConnectionManager.testConnection();

            try (Connection connection = ConnectionManager.getConnection()) {
                MigrationManager migrationManager = new MigrationManager(connection);
                migrationManager.runMigrations();
            }
        } catch (SQLException e) {
            log.error("Ошибка при работе с базой данных: {}", e.getMessage(), e);
        } catch (IllegalAccessException e) {
            log.warn("Миграции не были запущены: {}", e.getMessage());
        } catch (RuntimeException e) {
            log.error("Непредвиденная ошибка выполнения программы: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Ошибка при выполнении миграции", e);
        }
    }
}
