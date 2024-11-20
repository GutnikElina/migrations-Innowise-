package db;

import migrations.MigrationFileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MigrationManager {

    private static final String MIGRATION_TABLE = "migration_history";
    private final Connection connection;
    private final MigrationExecutor executor;

    public MigrationManager(Connection connection) {
        this.connection = connection;
        this.executor = new MigrationExecutor(connection);
    }

    private void ensureMigrationTableExists() throws Exception {
        String createTableSql = "CREATE TABLE IF NOT EXISTS " + MIGRATION_TABLE + " (" +
                "id SERIAL PRIMARY KEY, " +
                "file_name VARCHAR(255) NOT NULL UNIQUE, " +
                "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        executor.execute(createTableSql);
    }

    private boolean isMigrationApplied(String fileName) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + MIGRATION_TABLE + " WHERE file_name = '" + fileName + "'";
        try (var stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    private void applyMigration(String fileName, String sql) throws Exception {
        try {
            executor.execute(sql);
            executor.logMigration(fileName);
        } catch (SQLException e) {
            System.err.println("Не удалось применить миграцию: " + fileName);
            throw e;
        }
    }

    private void acquireLock() throws SQLException {
        String lockSql = "SELECT pg_advisory_lock(1)";
        executor.execute(lockSql);
    }

    private void releaseLock() throws SQLException {
        String unlockSql = "SELECT pg_advisory_unlock(1)";
        executor.execute(unlockSql);
    }

    public void runMigrations() throws Exception {
        try {
            acquireLock();
            ensureMigrationTableExists();

            MigrationFileReader fileReader = new MigrationFileReader();
            List<String> migrationFiles = fileReader.findMigrationFiles("migrations");
            for (String file : migrationFiles) {
                if (!isMigrationApplied(file)) {
                    System.out.println("Применение миграции: " + file);
                    String sql = fileReader.readMigrationFile(file);
                    applyMigration(file, sql);
                }
            }
        } finally {
            releaseLock();
        }
    }
}

