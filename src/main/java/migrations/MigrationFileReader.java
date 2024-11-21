package migrations;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MigrationFileReader {

    public List<String> findMigrationFiles(String path) throws Exception {
        var resources = getClass().getClassLoader().getResources(path);
        List<String> migrationFiles = new ArrayList<>();

        while (resources.hasMoreElements()) {
            var url = resources.nextElement();
            try (var files = Files.walk(Paths.get(url.toURI()))) {
                migrationFiles.addAll(files.filter(Files::isRegularFile)
                        .map(file -> file.getFileName().toString())
                        .filter(f -> f.matches("V\\d+.*\\.sql"))
                        .toList());
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

    public String readMigrationFile(String filePath) throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            if (in == null) throw new RuntimeException("Файл миграции не найден: " + filePath);
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
