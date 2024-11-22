package migrations;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            if (in == null) {
                log.error("Файл миграции не найден: {}", filePath);
                throw new IOException("Файл миграции отсутствует: " + filePath);
            }
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            log.error("Ошибка чтения файла миграции: {}", filePath, e);
            throw new IOException("Ошибка при чтении файла миграции " + filePath, e);
        }
    }

    /**
     * Находит все файлы миграций в заданном каталоге.
     *
     * @param path путь к каталогу с миграциями
     * @return список имен файлов миграций
     * @throws Exception если возникает ошибка при поиске файлов
     */
    public List<String> findMigrationFiles(String path) throws Exception {
        var resources = getClass().getClassLoader().getResources(path);
        List<String> migrationFiles = new ArrayList<>();

        while (resources.hasMoreElements()) {
            var url = resources.nextElement();
            try (var files = Files.walk(Paths.get(url.toURI()))) {
                migrationFiles.addAll(files.filter(Files::isRegularFile)
                        .map(file -> file.getFileName().toString())
                        .filter(f -> f.matches("V\\d+.*\\.sql")).toList());
            }
        }
        migrationFiles.sort(this::compareVersions);
        log.debug("Найдено {} миграционных файлов.", migrationFiles.size());
        return migrationFiles;
    }

    private int compareVersions(String f1, String f2) {
        int version1 = Integer.parseInt(f1.split("__")[0].substring(1));
        int version2 = Integer.parseInt(f2.split("__")[0].substring(1));
        return Integer.compare(version1, version2);
    }
}
