package com.library;

import com.library.migrations.MigrationTool;
import lombok.extern.slf4j.Slf4j;

/**
 * Главный класс для запуска процесса миграции базы данных. Он считывает аргументы командной строки,
 * проверяет их корректность и передает выполнение соответствующей команды в {@link MigrationTool}.
 */
@Slf4j
public class Main {

    /**
     * Точка входа для приложения. Cчитывает аргументы командной строки, проверяет их
     * корректность и передает выполнение соответствующей команды в {@link MigrationTool}.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            log.error("No command specified. Available commands: migrate, rollback, status");
            return;
        }

        String command = args[0];
        try {
            MigrationTool.executeMigration(command);
        } catch (Exception e) {
            log.error("Error executing migration: {}", e.getMessage(), e);
        }
    }
}