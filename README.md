# Библиотека для управления миграциями баз данных

## Описание
Проект предназначен для управления миграциями базы данных, используя SQL-скрипты. Он позволяет выполнять миграции, откатить их и проверять статус 
миграций с помощью простых команд через консоль(CLI-утилита). А также позволяет использовать этот проект как зависимость в другом проекте(использование JitPack как удаленного репозитория)

  ## Особенности

- **migrate**: Применяет все ожидающие миграции к базе данных.
- **rollback**: Откатывает последнюю примененную миграцию.
- **status**: Проверяет статус примененных миграций.

## Требования

- **Java 17** или выше.
- База данных **PostgreSQL** (ниже указаны действия , как установить PostreSQL при её отсутсвии через **Docker**)
- **Maven** (для сборки и управления зависимостями).

## Запуск проекта через командную строку(первый способ)

1. Откройте терминал для выполнения команд. Перейдите в директорию, в которую хотите клонировать удаленный репозиторий (с помощью команды cd).

2. Клонирование репозитория:
   ```bash
   git clone https://github.com/GutnikElina/migrations-Innowise-.git

3. Переход в директорию проекта:
   ```bash
   cd migrations-Innowise-

4. Сборка проекта с помощью Maven:
   ```bash
   mvn clean package
Это создаст JAR файл со всеми зависимостями в директории `target`.

## Установка PostgreSQL(если отсутствует) с помощью Docker

Откройте терминал для выполнения команд.
Примечание: Убедитесь, что Docker Desktop запущен перед выполнением следующих шагов.
```
  docker run --name postgres-container -e POSTGRES_USER=root -e POSTGRES_PASSWORD=root -e POSTGRES_DB=migrations_db -p 5432:5432 -d postgres
```
После чего application.properties можно не задавать в проекте поскольку они уже есть по умолчанию. 
**Примечание:** 
Но если в команде ```docker run``` укажете другие данные (логин, пароль или база данных), то придется создавать файл application.properties с вашими данными.

## Если PostgreSQL уже был установлен

Замените в клонированном проекте в пакете ```resources``` файл ```application.properties``` на свои значения для открытия бд PostgreSQL:
```
  db.url=jdbc:postgresql://localhost:5432/{БД_ДЛЯ_ЗАПИСИ_МИГРАЦИОННЫХ_ФАЙЛОВ}
  db.username={ВАШ_ЛОГИН}
  db.password={ВАШ_ПАРОЛЬ}
```
Для запуска проекта потребуется использовать в терминале следующие команды:

5. **Запуск приложения**
    Вы можете использовать команду `java -jar`, чтобы запустить проект и выполнить команды миграции.

**Применение миграций:**
Для применения всех ожидающих миграций:

```bash
    java -jar target/migrations_project-2.2-SNAPSHOT.jar migrate
 ```

**Откат последней миграции:**
Для отката последней примененной миграции:

```bash
    java -jar target/migrations_project-2.2-SNAPSHOT.jar rollback
```

**Проверка статуса миграций:**
Для проверки статуса примененных миграций:

```bash
    java -jar target/migrations_project-2.2-SNAPSHOT.jar status
```
### Аргументы командной строки

Поддерживаются следующие команды:

- `migrate`: Применяет все ожидающие миграции.
- `rollback`: Откатывает последнюю примененную миграцию.
- `status`: Показывает статус примененных миграций.

Если аргументы не указаны, приложение выведет сообщение об ошибке и завершит работу.

## Добавление как зависимость в другой проект (второй способ)
Если вы хотите использовать этот проект как библиотеку в вашем собственном Java проекте, вы можете добавить его как зависимость в ваш файл `pom.xml`.

### 1: Добавьте репозиторий

Проект размещен на репозитории JitPack, поэтому добавьте определение репозитория в ваш файл `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
### 2: Добавьте зависимость
```xml
<dependency>
    <groupId>com.github.GutnikElina</groupId>
    <artifactId>migrations-Innowise-</artifactId>
    <version>v2.1</version>
</dependency>
```
### 3: Добавьте конфигурацию (или использование application.properties из jar файла)

Добавьте в проекте в пакете ```resources``` файл ```application.properties``` на свои значения для открытия PostgreSQL:
```
  db.url=jdbc:postgresql://localhost:5432/{БД_ДЛЯ_ЗАПИСИ_МИГРАЦИОННЫХ_ФАЙЛОВ}
  db.username={ВАШ_ЛОГИН}
  db.password={ВАШ_ПАРОЛЬ}
```
Если файл ```application.properties``` отсутствует в проекте, то значения для подключения PostgreSQL будут взяты по умолчанию из jar файла.

### 3: После чего нужно заново пересобрать ваш проект:
   ```bash
   mvn clean package
   ```

## Пример использования библиотеки для управления миграциями как зависимость в другом проекте

Вы можете использовать класс MigrationTool для выполнения миграций программно. Вот пример вызова команд миграции:
```
import com.library.migrations.MigrationTool;

public class YourApp {
    public static void main(String[] args) {
        try {
            MigrationTool.executeMigration("migrate");   //"status" - для списка примененных миграций, "rollback" - для отката
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
