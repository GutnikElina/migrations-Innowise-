package com.library.migrations;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Класс для чтения и обработки файлов миграций.
 * Он отвечает за чтение миграционных SQL-файлов и поиск доступных миграций.
 */
@Slf4j
public class MigrationFileReader {

    /**
     * Читает файл миграции с заданным путем
     *
     * @param filePath путь к файлу миграции
     * @return содержимое файла в виде строки
     * @throws IOException если возникает ошибка при чтении файла
     */
    public String readMigrationFile(String filePath) throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(filePath);
        if (in == null) {
            log.error("Migration file not found: {}", filePath);
            throw new IOException("Migration file not found: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            log.error("Error reading migration file: {}", filePath, e);
            throw new IOException("Error reading migration file " + filePath, e);
        }
    }

    /**
     * Находит все файлы миграций в заданном каталоге.
     *
     * @param path путь к каталогу с миграциями
     * @return список имен файлов миграций
     * @throws IOException если возникает ошибка при поиске файлов
     * @throws URISyntaxException если возникает ошибка при работе с URI
     */
    public List<String> findMigrationFiles(String path) throws IOException, URISyntaxException {
        List<String> migrationFiles = new ArrayList<>();

        var classLoader = getClass().getClassLoader();
        var resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            var url = resources.nextElement();
            if ("jar".equals(url.getProtocol())) {
                //для JAR-файлов
                String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
                try (var jarFile = new JarFile(jarPath)) {
                    var entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        var entry = entries.nextElement();
                        String entryName = entry.getName();
                        if (entryName.startsWith(path) && entryName.endsWith(".sql") && entryName.matches(".*/V\\d+.*\\.sql")) {
                            migrationFiles.add(entryName.substring(path.length() + 1));
                        }
                    }
                }
            } else {
                //для файловой системы (например, в процессе разработки)
                try (var files = Files.walk(Paths.get(url.toURI()))) {
                    migrationFiles.addAll(
                            files.filter(Files::isRegularFile)
                                    .map(file -> file.getFileName().toString())
                                    .filter(f -> f.matches("V\\d+.*\\.sql"))
                                    .toList());
                }
            }
        }
        migrationFiles.sort(this::compareVersions);
        log.debug("Found {} migration files.", migrationFiles.size());
        return migrationFiles;
    }


    private int compareVersions(String f1, String f2) {
        try {
            String[] parts1 = f1.split("__");
            String[] parts2 = f2.split("__");
            if (parts1.length == 0 || parts2.length == 0) {
                throw new IllegalArgumentException("Invalid migration file format: " + f1 + " or " + f2);
            }
            int version1 = Integer.parseInt(parts1[0].substring(1));
            int version2 = Integer.parseInt(parts2[0].substring(1));
            return Integer.compare(version1, version2);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid version format in migration files: " + f1 + ", " + f2, e);
        }
    }
}
