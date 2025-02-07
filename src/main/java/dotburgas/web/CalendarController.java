package dotburgas.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Controller
public class CalendarController {


    @GetMapping("/calendar")
    public String showCalendarPage() {
        return "calendar";
    }

    @PostMapping("/saveData")
    public Map<String, String> saveData(@RequestBody Map<String, String> request) {

        String selectedDate = request.get("date");
        return Map.of("date", selectedDate, "status", "success");
    }
}
