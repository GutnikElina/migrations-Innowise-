package migrations;

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
        log.debug("MigrationManager создан.");
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
                    log.info("Применение миграции: {}", file);
                    String sql = fileReader.readMigrationFile(filePath);
                    applyMigration(file, sql);
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка SQL во время миграции: {}", e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            log.error("Ошибка чтения файла миграции: {}", e.getMessage(), e);
            throw e;
        } finally {
            releaseLock();
        }
    }

    private void ensureMigrationTableExists() throws SQLException {
        executor.execute(CREATE_TABLE_SQL);
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
        } catch (NullPointerException e) {
            log.error("Не удалось применить миграцию: {}. Объект был null.", fileName, e);
            throw e;
        }
    }

    private void acquireLock() throws SQLException {
        String tryLockSql = "SELECT pg_try_advisory_lock(1)";
        //pg_try_advisory_lock(1) - получает исключительную блокировку на уровне сеанса, если это возможно
        //true - если блокировка захвачена
        //false - блокировка занята
        log.info("Начало попытки приобретения блокировки...");
        try (var stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(tryLockSql)) {
            if (rs.next() && !rs.getBoolean(1)) {
                log.warn("Не удалось получить блокировку для миграций. Возможно, миграции уже выполняются " +
                        "другим процессом.");
                throw new IllegalStateException("Миграции уже выполняются. Повторите попытку позже.");
            }
        }
        log.info("Блокировка для миграций успешно приобретена.");
    }

    private void releaseLock() throws SQLException {
        String unlockSql = "SELECT pg_advisory_unlock(1)";
        try (var stmt = connection.createStatement()) {
            log.info("Начало попытки освобождения блокировки...");
            stmt.execute(unlockSql);
            log.info("Блокировка для миграций успешно освобождена.");
        } catch (SQLException e) {
            log.error("Ошибка при освобождении блокировки: {}", e.getMessage(), e);
            throw e;
        }
    }
}
