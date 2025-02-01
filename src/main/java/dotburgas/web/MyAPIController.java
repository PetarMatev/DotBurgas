package dotburgas.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1")
public class MyAPIController {

    @GetMapping("/info/1")
    public String getInfo1(HttpServletRequest request, HttpServletResponse response) {

        String author = request.getHeader("Author");
        response.addHeader("Applicaiton-Name", "Spring Boot App, Dot Burgas");

        Cookie cookie1 = new Cookie("color", "Blue");
        Cookie cookie2 = new Cookie("city", "Sofia");
        Cookie cookie3 = new Cookie("country", "Bulgaria");
        response.addCookie(cookie1);
        response.addCookie(cookie2);
        response.addCookie(cookie3);

        return "hello " + author + "!";
    }

    @GetMapping("/info/2")
    public String getInfo2(@CookieValue(value = "color") String color, @CookieValue(value = "city") String city) {


        return "color " + color + " " + city;
    }

}
