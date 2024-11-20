package db;

import config.PropertiesUtils;
import java.sql.Connection;

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
            e.printStackTrace();
        }
    }
}