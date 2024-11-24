package com.library.migrations;

import com.library.config.ConnectionManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Класс, отвечающий за выполнение миграций.
 */
@Slf4j
public class MigrationTool {

    /**
     * Метод для выполнения команды миграции.
     *
     * @param command команда: migrate, rollback, status
     * @throws SQLException если возникает ошибка при работе с базой данных
     * @throws IOException если возникает ошибка при работе с файлами
     */
    public static void executeMigration(String command) throws SQLException, IOException, URISyntaxException {
        ConnectionManager.testConnection();

        try (Connection connection = ConnectionManager.getConnection()) {
            MigrationManager migrationManager = new MigrationManager(connection);

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
    }
}