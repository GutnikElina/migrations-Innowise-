package migrations;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@AllArgsConstructor
public class MigrationExecutor {
    private final Connection connection;

    public void execute(String sql) throws SQLException {
        try (var stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            stmt.execute(sql);
            connection.commit();
            log.info("SQL-запрос успешно выполнен: {}", sql);
        } catch (SQLException e) {
            connection.rollback();
            log.error("Не удалось выполнить SQL-запрос: {}", sql, e);
            throw new SQLException("Не удалось выполнить SQL-запрос: " + sql, e);
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void logMigration(String migrationFileName) throws SQLException {
        String logSql = "INSERT INTO migration_history (file_name) VALUES ('" + migrationFileName + "')";
        execute(logSql);
        log.info("Миграция {} зафиксирована в базе данных", migrationFileName);
    }
}
