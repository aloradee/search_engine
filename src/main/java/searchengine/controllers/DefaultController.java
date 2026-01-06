package searchengine.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контроллер для обслуживания главной страницы веб-интерфейса.
 * Перенаправляет корневой URL на HTML-страницу.
 *
 * @author Кирилл Христич
 */
@Controller
public class DefaultController {

    /**
     * Обрабатывает корневой URL и возвращает главную страницу.
     *
     * @return имя HTML-шаблона (index.html)
     */
    @RequestMapping("/")
    public String index() {
        return "index";
    }
}
