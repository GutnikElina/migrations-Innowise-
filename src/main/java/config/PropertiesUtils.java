package config;

import lombok.extern.slf4j.Slf4j;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

@Slf4j
public class PropertiesUtils {

    private final Properties properties;

    public PropertiesUtils(String resourcePath) throws Exception {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new FileNotFoundException("Файл конфигурации не найден: " + resourcePath);
            }
            properties.load(input);
            log.info("Конфигурация загружена из ресурса: {}", resourcePath);
        } catch (FileNotFoundException e) {
            log.error("Не удалось найти файл конфигурации: {}", resourcePath, e);
            throw e;
        }
    }

    public PropertiesUtils(Path path) throws Exception {
        properties = new Properties();
        try (InputStream input = new FileInputStream(path.toFile())) {
            properties.load(input);
            log.info("Конфигурация загружена из файла: {}", path);
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}