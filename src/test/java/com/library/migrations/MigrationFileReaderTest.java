package com.library.migrations;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MigrationFileReaderTest {

    private final MigrationFileReader fileReader = new MigrationFileReader();

    @Test
    void testReadMigrationFile_Success() throws IOException {
        String filePath = "migrations/V1__Create_table.sql";
        String content = fileReader.readMigrationFile(filePath);
        assertNotNull(content, "Content of the migration file shouldn't be null!");
        assertFalse(content.isEmpty(), "Content of the migration file shouldn't be empty!");
    }

    @Test
    void testReadMigrationFile_FileNotFound() {
        assertThrows(NullPointerException.class, () -> fileReader.readMigrationFile("migrations/nonexistent.sql"),
                "Should throw IOException for nonexistent files!");
    }

    @Test
    void testFindMigrationFiles_Success() throws Exception {
        List<String> migrationFiles = fileReader.findMigrationFiles("migrations");
        assertNotNull(migrationFiles, "Migration files list shouldn't be null!");
        assertFalse(migrationFiles.isEmpty(), "There should be at least one migration file!");
    }
}
