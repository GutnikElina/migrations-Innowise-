package config;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PropertiesUtils {

    private static final Properties properties = new Properties();

    static {
        try {
            String propertiesPath = "application.properties";
            try (InputStream input = PropertiesUtils.class.getClassLoader().getResourceAsStream(propertiesPath)) {
                if (input == null) {
                    throw new IOException("Файл конфигурации не найден в ресурсах: " + propertiesPath);
                }
                properties.load(input);
                log.info("Конфигурация загружена из ресурса: {}", propertiesPath);
            }
        } catch (IOException e) {
            log.error("Ошибка загрузки конфигурации: ", e);
            throw new RuntimeException("Ошибка загрузки конфигурации", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
