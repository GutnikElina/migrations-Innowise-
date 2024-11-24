package com.library.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PropertiesUtilsTest {

    @Test
    void testGetPropertyWithExistingKey() {
        String dbUrl = PropertiesUtils.get("db.url");
        assertNotNull(dbUrl, "Property 'db.url' shouldn't be null");
    }

    @Test
    void testGetPropertyWithDefaultValue() {
        String defaultValue = PropertiesUtils.get("notExisting.key", "default");
        assertEquals("default", defaultValue, "Default value should be returned if the key doesn't exist");
    }

    @Test
    void testGetPropertyWithoutDefaultValue() {
        String nonExistingValue = PropertiesUtils.get("notExisting.key");
        assertNull(nonExistingValue, "Should return null for a non-existing key without default");
    }
}
