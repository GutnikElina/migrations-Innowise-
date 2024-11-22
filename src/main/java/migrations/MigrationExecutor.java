package migrations;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Класс для выполнения SQL-миграций и записи их в историю.
 * Обрабатывает выполнение SQL-запросов и добавление записей о выполненных миграциях
 */
@Slf4j
@AllArgsConstructor
public class MigrationExecutor {

    private final Connection connection;
    private static final String MIGRATION_TABLE = "migration_history";

    /**
     * Выполняет SQL-запрос
     *
     * @param sql SQL-запрос для выполнения
     * @throws SQLException если возникает ошибка при выполнении SQL-запроса
     */
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

    /**
     * Добавляет запись о выполненной миграции в таблицу истории миграций
     *
     * @param migrationFileName имя файла миграции
     * @throws SQLException если возникает ошибка при добавлении записи в таблицу
     */
    public void logMigration(String migrationFileName) throws SQLException {
        String logSql = "INSERT INTO " + MIGRATION_TABLE + " (file_name) VALUES (?)";
        try (var pstmt = connection.prepareStatement(logSql)) {
            pstmt.setString(1, migrationFileName);
            pstmt.executeUpdate();
            log.info("Миграция {} зафиксирована в базе данных", migrationFileName);
        }
    }
}
