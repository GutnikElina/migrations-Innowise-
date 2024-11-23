package com.library.config;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionManagerTest {

    @Test
    void testGetConnection() throws SQLException {
        try (Connection connection = ConnectionManager.getConnection()) {
            assertNotNull(connection);
            assertTrue(connection.isValid(2));
        }
    }

    @Test
    void testTestConnection() {
        assertDoesNotThrow(ConnectionManager::testConnection);
    }
}