package migrations;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
@AllArgsConstructor
public class MigrationExecutor {

    private final Connection connection;
    private static final String MIGRATION_TABLE = "migration_history";

    public void execute(String sql) throws SQLException {
        boolean initialAutoCommit = connection.getAutoCommit();
        try (var stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            stmt.execute(sql);
            connection.commit();
            log.info("SQL-запрос успешно выполнен: {}", sql);
        } catch (SQLException e) {
            connection.rollback();
            log.error("Ошибка при выполнении SQL-запроса: {}", sql, e);
            throw e;
        } finally {
            connection.setAutoCommit(initialAutoCommit);
        }
    }

    public void logMigration(String migrationFileName) throws SQLException {
        String logSql = "INSERT INTO " + MIGRATION_TABLE + " (file_name) VALUES (?)";
        try (var pstmt = connection.prepareStatement(logSql)) {
            pstmt.setString(1, migrationFileName);
            pstmt.executeUpdate();
            log.info("Миграция {} зафиксирована в базе данных", migrationFileName);
        }
    }
}
