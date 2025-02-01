package dotburgas.web;

import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller

public class WalletController {

    private final UserService userService;

    public WalletController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/wallets")
    public String getWalletsPage() {

        return "wallets";
    }


    @GetMapping("/wallets/home")
    public ModelAndView getHomePage(HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        modelAndView.addObject(user);

        return modelAndView;
    }
}
