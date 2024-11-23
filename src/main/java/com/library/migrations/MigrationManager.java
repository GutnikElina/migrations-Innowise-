package com.library.migrations;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Класс, управляющий процессом миграции базы данных.
 * Он обрабатывает выполнение миграций, создает таблицу для отслеживания миграций,
 * а также управляет блокировками для предотвращения параллельного выполнения миграций
 */
@Slf4j
public class MigrationManager {

    private static final String MIGRATION_TABLE = "migration_history";
    private static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS migration_history (
            id SERIAL PRIMARY KEY,
            file_name VARCHAR(255) NOT NULL UNIQUE,
            applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
        """;
    private final Connection connection;
    private final MigrationExecutor executor;

    /**
     * Конструктор для инициализации менеджера миграции с использованием соединения с базой данных
     *
     * @param connection соединение с базой данных
     */
    public MigrationManager(Connection connection) {
        this.connection = connection;
        this.executor = new MigrationExecutor(connection);
        log.debug("MigrationManager created.");
    }

    /**
     * Запускает все миграции, проверяя, были ли они уже применены.
     * Обрабатывает создание таблицы миграции и выполнение миграций для каждого файла
     *
     * @throws Exception если возникает ошибка при применении миграций
     */
    public void runMigrations() throws Exception {
        acquireLock();
        try {
            ensureMigrationTableExists();

            MigrationFileReader fileReader = new MigrationFileReader();
            List<String> migrationFiles = fileReader.findMigrationFiles("migrations");
            for (String file : migrationFiles) {
                if (!isMigrationApplied(file)) {
                    String filePath = "migrations/" + file;
                    log.info("Applying migration: {}", file);
                    String sql = fileReader.readMigrationFile(filePath);
                    applyMigration(file, sql);
                }
            }
        } catch (SQLException e) {
            log.error("Error SQL during migration: {}", e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            log.error("Error reading migration file: {}", e.getMessage(), e);
            throw e;
        } finally {
            releaseLock();
        }
    }

    /**
     * Выводит в лог статус всех миграций, примененных к базе данных.
     * Каждая запись содержит название файла миграции и дату/время ее применения
     *
     * @throws SQLException если возникает ошибка при выполнении SQL-запроса
     */
    public void printMigrationStatus() throws SQLException {
        String query = "SELECT file_name, applied_at FROM " + MIGRATION_TABLE + " ORDER BY applied_at DESC";
        try (var stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            log.info("Migration status:");
            while (rs.next()) {
                log.info("Migration: {}; applied at: {}", rs.getString("file_name"), rs.getTimestamp("applied_at"));
            }
        }
    }

    /**
     * Выполняет откат последней примененной миграции. Находит последнюю
     * примененную миграцию, ищет соответствующий файл отката и выполняет его.
     * Удаляет запись о последней миграции из таблицы истории миграций.
     *
     * @throws SQLException если возникает ошибка при выполнении SQL-запроса или отката
     */
    public void rollbackMigration() throws SQLException {
        String lastAppliedMigration = getLastAppliedMigration();
        if (lastAppliedMigration == null) {
            log.warn("No migrations to roll back.");
            return;
        }

        String rollbackFile = lastAppliedMigration.replaceFirst("^V", "U");
        String rollbackFilePath = "migrations/" + rollbackFile;

        try {
            MigrationFileReader fileReader = new MigrationFileReader();
            log.info("Reading rollback file for migration: {}", rollbackFile);
            String rollbackSql = fileReader.readMigrationFile(rollbackFilePath);

            executor.execute(rollbackSql);

            String deleteSql = "DELETE FROM " + MIGRATION_TABLE + " WHERE file_name = ?";
            try (var pstmt = connection.prepareStatement(deleteSql)) {
                pstmt.setString(1, lastAppliedMigration);
                pstmt.executeUpdate();
            }
            log.info("Rollback of the last migration performed: {}", lastAppliedMigration);
        } catch (IOException e) {
            log.error("Rollback file for migration {} not found: {}", lastAppliedMigration, rollbackFilePath, e);
            throw new SQLException("Couldn't find rollback file: " + rollbackFilePath, e);
        } catch (SQLException e) {
            log.error("Error executing rollback for migration {}: {}", lastAppliedMigration, rollbackFilePath, e);
            throw e;
        }
    }

    private void ensureMigrationTableExists() throws SQLException {
        executor.execute(CREATE_TABLE_SQL);
        log.info("Migration table checked/created");
    }

    private boolean isMigrationApplied(String fileName) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + MIGRATION_TABLE + " WHERE file_name = '" + fileName + "'";
        try (var stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            rs.next();
            boolean applied = rs.getInt(1) > 0;
            log.debug("Migration {} applied: {}", fileName, applied);
            return applied;
        }
    }

    private void applyMigration(String fileName, String sql) throws Exception {
        try {
            executor.execute(sql);
            executor.logMigration(fileName);
            log.info("Migration {} applied.", fileName);
        } catch (SQLException e) {
            log.error("Failed to apply migration: {}", fileName, e);
            throw e;
        } catch (NullPointerException e) {
            log.error("Attempting to rollback migration: {}. Object was null.", fileName, e);
            throw e;
        }
    }

    private void acquireLock() throws SQLException {
        String tryLockSql = "SELECT pg_try_advisory_lock(1)";
        //pg_try_advisory_lock(1) - получает исключительную блокировку на уровне сеанса, если это возможно
        //true - если блокировка захвачена
        //false - блокировка занята
        log.info("Starting attempt to acquire lock...");
        try (var stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(tryLockSql)) {
            if (rs.next() && !rs.getBoolean(1)) {
                log.warn("Failed to acquire lock for migrations. Possibly, migrations are already running by another process.");
                throw new IllegalStateException("Migrations are already running. Please try again later");
            }
        }
        log.info("Lock acquired successfully for migrations.");
    }

    private void releaseLock() throws SQLException {
        String unlockSql = "SELECT pg_advisory_unlock(1)";
        try (var stmt = connection.createStatement()) {
            log.info("Starting attempt to release lock...");
            stmt.execute(unlockSql);
            log.info("Lock successfully released.");
        } catch (SQLException e) {
            log.error("Error releasing lock: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String getLastAppliedMigration() throws SQLException {
        String query = "SELECT file_name FROM " + MIGRATION_TABLE + " ORDER BY applied_at DESC LIMIT 1";
        try (var stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getString("file_name");
            }
        }
        return null;
    }
}
