import config.ConnectionManager;
import config.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import migrations.MigrationManager;
import java.sql.Connection;

@Slf4j
public class MigrationTool {

    public static void main(String[] args) {
        try {
            String propertiesPath = "application.properties";
            PropertiesUtils propertiesUtils = new PropertiesUtils(propertiesPath);
            ConnectionManager connectionManager = new ConnectionManager(propertiesUtils);

            try (Connection connection = connectionManager.getConnection()) {
                MigrationManager migrationManager = new MigrationManager(connection);
                migrationManager.runMigrations();
            }
        } catch (Exception e) {
            log.error("Ошибка при выполнении миграции", e);
        }
    }
}
