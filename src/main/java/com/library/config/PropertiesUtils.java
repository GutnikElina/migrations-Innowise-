package com.library.config;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Утилитный класс для работы с конфигурационными файлами(в данном случае из ресурсов).
 * Предоставляет методы для загрузки конфигурации и извлечения значений по ключам
 */
@Slf4j
public class PropertiesUtils {

    private static final Properties properties = new Properties();

    static {
        try {
            String propertiesPath = "application.properties";
            try (InputStream input = PropertiesUtils.class.getClassLoader().getResourceAsStream(propertiesPath)) {
                if (input == null) {
                    throw new IOException("Configuration file not found in resources: " + propertiesPath);
                }
                properties.load(input);
                log.info("Configuration loaded from resource: {}", propertiesPath);
            }
        } catch (IOException e) {
            log.error("Configuration loading error: ", e);
            throw new RuntimeException("Error loading configuration", e);
        }
    }

    /**
     * Получает значение свойства по ключу
     *
     * @param key ключ свойства.
     * @return значение свойства.
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * Получает значение свойства по ключу с указанием значения по умолчанию
     *
     * @param key ключ свойства.
     * @param defaultValue значение по умолчанию, если свойство не найдено.
     * @return значение свойства или значение по умолчанию, если свойство не найдено.
     */
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
