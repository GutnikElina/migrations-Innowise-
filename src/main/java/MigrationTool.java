import config.ConnectionManager;
import lombok.extern.slf4j.Slf4j;
import migrations.MigrationManager;
import java.sql.Connection;

@Slf4j
public class MigrationTool {

    public static void main(String[] args) {
        try {
            ConnectionManager.testConnection();

            try (Connection connection = ConnectionManager.getConnection()) {
                MigrationManager migrationManager = new MigrationManager(connection);
                migrationManager.runMigrations();
            }
        } catch (Exception e) {
            log.error("Ошибка при выполнении миграции", e);
        }
    }
}
