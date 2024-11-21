package migrations;

import lombok.extern.slf4j.Slf4j;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class MigrationManager {

    private static final String MIGRATION_TABLE = "migration_history";
    private final Connection connection;
    private final MigrationExecutor executor;

    public MigrationManager(Connection connection) {
        this.connection = connection;
        this.executor = new MigrationExecutor(connection);
        log.debug("MigrationManager создан.");
    }

    private void ensureMigrationTableExists() throws Exception {
        String createTableSql = "CREATE TABLE IF NOT EXISTS " + MIGRATION_TABLE + " (" +
                "id SERIAL PRIMARY KEY, " +
                "file_name VARCHAR(255) NOT NULL UNIQUE, " +
                "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        executor.execute(createTableSql);
        log.info("Миграционная таблица проверена/создана.");
    }

    private boolean isMigrationApplied(String fileName) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + MIGRATION_TABLE + " WHERE file_name = '" + fileName + "'";
        try (var stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            rs.next();
            boolean applied = rs.getInt(1) > 0;
            log.debug("Миграция {} применена: {}", fileName, applied);
            return applied;
        }
    }

    private void applyMigration(String fileName, String sql) throws Exception {
        try {
            executor.execute(sql);
            executor.logMigration(fileName);
            log.info("Миграция {} применена.", fileName);
        } catch (SQLException e) {
            log.error("Не удалось применить миграцию: {}", fileName, e);
            throw e;
        }
    }

    private void acquireLock() throws SQLException {
        String lockSql = "SELECT pg_advisory_lock(1)";
        executor.execute(lockSql);
        log.info("Блокировка для миграций приобретена.");
    }

    private void releaseLock() throws SQLException {
        String unlockSql = "SELECT pg_advisory_unlock(1)";
        executor.execute(unlockSql);
        log.info("Блокировка для миграций освобождена.");
    }

    public void runMigrations() throws Exception {
        try {
            acquireLock();
            ensureMigrationTableExists();

            MigrationFileReader fileReader = new MigrationFileReader();
            List<String> migrationFiles = fileReader.findMigrationFiles("migrations");
            for (String file : migrationFiles) {
                if (!isMigrationApplied(file)) {
                    String filePath = "migrations/" + file;
                    log.info("Применение миграции: {}", file);
                    String sql = fileReader.readMigrationFile(filePath);
                    applyMigration(file, sql);
                }
            }
        } finally {
            releaseLock();
        }
    }
}
