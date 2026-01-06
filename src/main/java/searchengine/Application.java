package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Основной класс Spring Boot приложения.
 * Содержит точку входа в программу.
 *
 * @author Кирилл Христич
 */
@SpringBootApplication
public class Application {
    /**
     * Точка входа в приложение.
     * Запускает Spring Boot приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
