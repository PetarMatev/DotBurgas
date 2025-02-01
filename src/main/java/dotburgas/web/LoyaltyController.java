package dotburgas.web;

import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("loyalties")
public class LoyaltyController {

    private final UserService userService;


    @Autowired
    public LoyaltyController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String getUpgradePage() {

        return "loyalties";
    }


    @GetMapping("/history")
    public ModelAndView getLoyaltyHistory(HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("loyalty-history");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
