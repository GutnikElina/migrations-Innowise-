import com.library.config.ConnectionManager;
import lombok.extern.slf4j.Slf4j;
import com.library.migrations.MigrationManager;

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
        if (args.length == 0) {
            log.error("No command specified. Available commands: migrate, rollback, status");
            return;
        }

        try {
            ConnectionManager.testConnection();

            try (Connection connection = ConnectionManager.getConnection()) {
                MigrationManager migrationManager = new MigrationManager(connection);
                String command = args[0];

                switch (command) {
                    case "migrate":
                        log.info("Starting migrations.....");
                        migrationManager.runMigrations();
                        break;

                    case "rollback":
                        log.info("Rollback of migrations...");
                        migrationManager.rollbackMigration();
                        break;

                    case "status":
                        log.info("Checking migration status...");
                        migrationManager.printMigrationStatus();
                        break;

                    default:
                        log.error("Unknown command: {}. Available commands: migrate, rollback, status", command);
                        break;
                }
            }
            log.info("Migration process successfully completed!");
        } catch (SQLException e) {
            log.error("Error working with the database: {}", e.getMessage(), e);
        } catch (IllegalAccessException e) {
            log.warn("Migrations were not started: {}", e.getMessage());
        } catch (RuntimeException e) {
            log.error("Unexpected runtime error: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error executing migration", e);
        }
    }
}
