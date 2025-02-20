package dotburgas.web;

import dotburgas.shared.security.AuthenticationDetails;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller

public class WalletController {

    private final UserService userService;

    @Autowired
    public WalletController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/wallets")
    public String getWalletsPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        authenticationDetails.getUserId();

        return "wallets";
    }


    @GetMapping("/wallets/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        modelAndView.addObject(user);

        return modelAndView;
    }
}
