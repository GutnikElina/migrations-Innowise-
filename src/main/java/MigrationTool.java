import config.ConnectionManager;
import lombok.extern.slf4j.Slf4j;
import migrations.MigrationManager;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class MigrationTool {

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
