package dotburgas.web;

import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
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
    public ModelAndView getHomePage() {

        User user = userService.getById(UUID.fromString("a56528be-12e7-4599-9c7a-75b81c4d7f77"));

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        modelAndView.addObject(user);

        return modelAndView;
    }
}
