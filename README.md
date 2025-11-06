# Search Engine - Поисковый движок

<h3>🎯Описание проекта:</h3>

<h4>Search Engine - это полнофункциональный поисковый движок, разработанный на Java с использованием Spring Boot. Система позволяет индексировать веб-сайты, обрабатывать контент с учетом морфологии русского языка и осуществлять релевантный поиск по проиндексированным данным.
Система имитирует работу современных поисковых систем (как Google или Yandex), но в упрощенном варианте.</h4>

<h3>🔍 Ключевые возможности:</h3>
<h4>Индексация:</h4>
1. Многопоточный обход сайтов с использованием Fork-Join Framework
2. Рекурсивный парсинг HTML-страниц с соблюдением robots.txt
3. Автоматическое определение кодировок и обработка ошибок
4. Контроль частоты запросов для избежания блокировок

<h4>Обработка контента:</h4>
1. Очистка HTML от тегов и скриптов
2. Морфологический анализ русского языка (лемматизация)
3. Удаление стоп-слов и служебных частей речи
4. Построение обратного индекса для быстрого поиска

<h4>Поиск:</h4>
1. Релевантный поиск с учетом морфологии
2. Ранжирование результатов по релевантности
3. Сниппеты с подсветкой найденных слов
4. Поиск по всем сайтам или конкретному домену

<h4>Управление:</h4>
1. Веб-интерфейс для мониторинга и управления
2. REST API для интеграции
3. Статистика в реальном времени
4. Управление процессами индексации

<h3>🏗️ Архитектура системы</h3>

<h4>Многослойная архитектура:</h4>


    Presentation Layer (Controllers)
        │
        ▼
    Business Layer (Services)
        │
        ▼   
    Data Access Layer (Repositories)
        │
        ▼
    Database Layer (MySQL)

<h3>🔧Стек технологий</h3>

<h3>Backend:</h3>

1. Java 17 - LTS версия с улучшенной производительностью
2. Spring Boot 3.2.0 - быстрый старт и автоконфигурация
3. Spring Data JPA - абстракция для работы с БД
4. Hibernate 6.3 - ORM с кэшированием и lazy loading
5. MySQL 8.0 - транзакционное хранилище InnoDB
6. Jsoup - парсинг HTML и CSS селекторы

<h4>Морфологический анализ:</h4>
1. Apache Lucene Morphology - морфологический анализ
2. RussianLuceneMorphology - для русского языка
3. EnglishLuceneMorphology - для английского языка

<h3>Frontend:</h3>

1. Thymeleaf - шаблонизатор для веб-страниц
2. HTML5/CSS3 - верстка интерфейса
3. JavaScript/jQuery - клиентская логика
4. Bootstrap - CSS фреймворк (если используется)

<h3>Инструменты:</h3>

1. Maven - управление зависимостями и сборка
2. Lombok - уменьшение boilerplate кода
3. IntelliJ IDEA - среда разработки (рекомендуемая)

<h3>⚙️ Принципы работы</h3>

<h4>Процесс индексации:</h4>
1. Инициализация - очистка старых данных, создание записей в БД
2. Обход страниц - многопоточный рекурсивный парсинг ссылок
3. Парсинг контента - извлечение текста из HTML
4. Лемматизация - приведение слов к нормальной форме
5. Построение индекса - расчет частот и релевантности

<h4>Алгоритм поиска:</h4>
1. Парсинг запроса - разбиение на слова и лемматизация
2. Фильтрация - удаление частых и стоп-слов
3. Поиск совпадений - поиск страниц содержащих все леммы
4. Ранжирование - расчет релевантности по TF-IDF
5. Формирование сниппетов - выделение контекста

<h3>🛠️Инструкция по локальному запуску</h3>

<h4>Предварительные требования:</h4>

<h4>1. Установите Java 17+:</h4>

    bash

    // Для Ubuntu/Debian

    sudo apt install openjdk-17-jdk

    // Для Windows: скачайте с oracle.com или используйте adoptium.net

<h4>2. Установите MySQL 8.0+:</h4>

    bash

    // Для Ubuntu/Debian

    sudo apt install mysql-server

    // Для Windows: скачайте с dev.mysql.com

<h4>3. Установите Maven:</h4>

    bash

    # Для Ubuntu/Debian

    sudo apt install maven
    
    # Для Windows: скачайте с maven.apache.org

<h3>Настройка базы данных</h3>

<h4>1. Подключитесь к MySQL:</h4>

    bash

    mysql -u root -p

<h4>2. Создайте базу данных:</h4>

    sql

    CREATE DATABASE search_engine CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

<h4>3. Создайте пользователя (опционально):</h4>

    sql

    CREATE USER 'search_user'@'localhost' IDENTIFIED BY 'search_password';
    GRANT ALL PRIVILEGES ON search_engine.* TO 'search_user'@'localhost';
    FLUSH PRIVILEGES;

<h3>Настройка приложения</h3>

<h4>1. Клонируйте или скачайте проект</h4>
<h4>2. Настройте файл application.yml:<br>
Создайте файл src/main/resources/application.yml:</h4>

    yaml
    
    server:
        port: 8080

    spring:
        datasource:
            url: jdbc:mysql://localhost:3306/search_engine?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
            username: root                    # или ваш пользователь
            password: your_mysql_password     # ваш пароль MySQL
            driver-class-name: com.mysql.cj.jdbc.Driver

        jpa:
            hibernate:
                ddl-auto: update
            show-sql: true
            properties:
                hibernate:
                    dialect: org.hibernate.dialect.MySQLDialect

    indexing-settings:
        sites:
            - url: https://www.playback.ru/
            name: PlayBack.Ru
            - url: https://volochek.life/
            name: Volochek
            - url: https://ipfran.ru/
            name: IPFRAN
        user-agent: SearchEngineBot/1.0
        referrer: https://www.google.com
        delay-between-requests: 1000

<h4>3. Настройте Maven settings.xml (для морфологических библиотек):<br>
Создайте/отредактируйте ~/.m2/settings.xml:</h4>

    xml

    <settings>
        <servers>
            <server>
                <id>skillbox-gitlab</id>
                <configuration>
                    <httpHeaders>
                        <property>
                            <name>Private-Token</name>
                            <value>wtb5axJDFX9Vm_W1Lexg</value>
                        </property>
                    </httpHeaders>
                </configuration>
            </server>
        </servers>

        <profiles>
            <profile>
                <id>default</id>
                <repositories>
                    <repository>
                        <id>skillbox-gitlab</id>
                        <url>https://gitlab.skillbox.ru/api/v4/projects/263574/packages/maven</url>
                    </repository>
                </repositories>
            </profile>
        </profiles>

        <activeProfiles>
            <activeProfile>default</activeProfile>
        </activeProfiles>
    </settings>

<h3>Сборка и запуск</h3>

<h4>1. Очистите кэш Maven:</h4>

    bash

    // Linux/Mac
    rm -rf ~/.m2/repository

    // Windows
    rmdir /s %USERPROFILE%\.m2\repository

<h4>2. Соберите проект</h4>

    bash

    mvn clean compile

<h4>3. Запустите проект</h4>

    bash

    // Способ 1: через Maven
    mvn spring-boot:run

    // Способ 2: через собранный JAR
    mvn clean package -DskipTests
    java -jar target/search-engine-1.0.0.jar

<h4>4. Откройте в браузере</h4>

    text
    
    http://localhost:8080/

<h3>📱Использование</h3>
<h4>Веб-интерфейс:</h4>

<h4>Главная страница (/) - статистика и общая информация</h4>

1. Статистика системы - общее количество сайтов, страниц и лемм
2. Детальная информация по каждому сайту:

    * Текущий статус индексации (INDEXING, INDEXED, FAILED)
    * Время последнего обновления статуса
    * Количество проиндексированных страниц
    * Количество уникальных лемм
    * Сообщения об ошибках (если есть)
3. Автообновление данных каждые 30 секунд
4. Визуальные индикаторы статусов работы системы

<h4>Панель управления</h4>
1. Запуск полной индексации - кнопка "Start Indexing"
    * Запускает процесс обхода всех сайтов из конфигурации
    * Автоматически очищает предыдущие данные
    * Работает в многопоточном режиме
    * Отображает прогресс в реальном времени
2. Остановка индексации - кнопка "Stop Indexing"
    * Корректно останавливает все процессы
    * Сохраняет текущее состояние
    * Устанавливает статус FAILED для прерванных сайтов
3. Индексация отдельной страницы - форма ввода URL
    * Добавление/обновление конкретной страницы
    * Проверка принадлежности к настроенным сайтам
    * Мгновенное обновление поискового индекса

<h4>Поисковая система</h4>
1. Поле ввода запроса - поддержка сложных запросов
2. Выбор сайта - выпадающий список для фильтрации
3. Пагинация - настройка количества результатов (limit/offset)
4. Визуальные результаты:
    * Заголовок страницы со ссылкой
    * URL источника
    * Сниппет с подсветкой найденных слов
    * Процент релевантности
    * Название сайта-источника

<h4>API endpoints:</h4>
1. GET /api/statistics - получение статистики<br>
2. GET /api/startIndexing - Запускает полную переиндексацию<br>
    * Возвращает: {"result": true} или ошибку<br>
    * Ошибка: {"result": false, "error": "Индексация уже запущена"}<br>
3. GET /api/stopIndexing<br>
    * Останавливает текущую индексацию<br>
    * Возвращает: {"result": true} или ошибку<br>
    * Ошибка: {"result": false, "error": "Индексация не запущена"}<br>

4. POST /api/indexPage


    bash 
    
    curl -X POST "http://localhost:8080/api/indexPage?url=https://example.com/page1"

* Параметры: url (обязательный)
* Индексирует одну страницу
* Проверяет принадлежность к настроенным доменам

<h4>Поисковые запросы</h4>
GET /api/search

    bash
    
    # Простой запрос
    curl "http://localhost:8080/api/search?query=программирование"

    # С фильтром по сайту
    curl "http://localhost:8080/api/search?query=java&site=https://example.com"

    # С пагинацией
    curl "http://localhost:8080/api/search?query=python&offset=10&limit=5"
<h3>📜Лицензия</h3>

<h4>Проект разработан в учебных целях.</h4>
