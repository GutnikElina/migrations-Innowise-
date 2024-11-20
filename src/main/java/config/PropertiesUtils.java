package config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public class PropertiesUtils {

    private final Properties properties;

    //для загрузки properties из resources
    public PropertiesUtils(String resourcePath) throws Exception {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new FileNotFoundException("Файл конфигурации не найден: " + resourcePath);
            }
            properties.load(input);
        }
    }

    //для загрузки из внешнего файла
    public PropertiesUtils(Path path) throws Exception {
        properties = new Properties();
        try (InputStream input = new FileInputStream(path.toFile())) {
            properties.load(input);
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
