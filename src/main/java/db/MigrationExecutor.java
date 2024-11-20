package db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MigrationExecutor {
    private final Connection connection;

    public MigrationExecutor(Connection connection) {
        this.connection = connection;
    }

    public void execute(String sql) throws SQLException {
        try (var stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            stmt.execute(sql);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("Не удалось выполнить SQL-запрос: " + sql, e);
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void logMigration(String migrationFileName) throws SQLException {
        String logSql = "INSERT INTO migration_history (file_name) VALUES ('" + migrationFileName + "')";
        execute(logSql);
    }
}
