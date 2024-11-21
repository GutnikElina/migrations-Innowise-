package migrations;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class MigrationManager {

    private static final String MIGRATION_TABLE = "migration_history";
    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + MIGRATION_TABLE + " (" +
            "id SERIAL PRIMARY KEY, " +
            "file_name VARCHAR(255) NOT NULL UNIQUE, " +
            "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    private static final String CHECK_MIGRATION_SQL = "SELECT COUNT(*) FROM " + MIGRATION_TABLE + " WHERE file_name = ?";
    private static final String ACQUIRE_LOCK_SQL = "SELECT pg_advisory_lock(1)";
    private static final String RELEASE_LOCK_SQL = "SELECT pg_advisory_unlock(1)";

    private final Connection connection;
    private final MigrationExecutor executor;

    public MigrationManager(Connection connection) {
        this.connection = connection;
        this.executor = new MigrationExecutor(connection);
        log.debug("MigrationManager создан.");
    }

    public void runMigrations() throws Exception {
        try {
            acquireLock();
            ensureMigrationTableExists();

            MigrationFileReader fileReader = new MigrationFileReader();
            List<String> migrationFiles = fileReader.findMigrationFiles("migrations");

            for (String file : migrationFiles) {
                if (!isMigrationApplied(file)) {
                    log.info("Применение миграции: {}", file);
                    String filePath = "migrations/" + file;
                    String sql = fileReader.readMigrationFile(filePath);
                    applyMigration(file, sql);
                }
            }
        } finally {
            releaseLock();
        }
    }

    private void ensureMigrationTableExists() throws SQLException {
        executor.execute(CREATE_TABLE_SQL);
        log.info("Миграционная таблица проверена/создана.");
    }

    private boolean isMigrationApplied(String fileName) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(CHECK_MIGRATION_SQL)) {
            pstmt.setString(1, fileName);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                boolean applied = rs.getInt(1) > 0;
                log.debug("Миграция {} уже применена: {}", fileName, applied);
                return applied;
            }
        }
    }

    private void applyMigration(String fileName, String sql) throws SQLException {
        executor.execute(sql);
        executor.logMigration(fileName);
        log.info("Миграция {} успешно применена.", fileName);
    }

    private void acquireLock() throws SQLException {
        executor.execute(ACQUIRE_LOCK_SQL);
        log.info("Блокировка для миграций установлена.");
    }

    private void releaseLock() throws SQLException {
        executor.execute(RELEASE_LOCK_SQL);
        log.info("Блокировка для миграций освобождена.");
    }
}