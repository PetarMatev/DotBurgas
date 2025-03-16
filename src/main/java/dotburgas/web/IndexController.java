package dotburgas.web;

import dotburgas.apartment.service.ApartmentService;
import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import dotburgas.web.dto.LoginRequest;
import dotburgas.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    private final UserService userService;
    private final ApartmentService apartmentService;

    @Autowired
    public IndexController(UserService userService, ApartmentService apartmentService) {
        this.userService = userService;
        this.apartmentService = apartmentService;
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParam) {

        ModelAndView modelAndView = new ModelAndView("login");
        // adding an empty DTO and setting up the attribute name.
        modelAndView.addObject("loginRequest", LoginRequest.builder().build());

        if (errorParam != null) {
            modelAndView.addObject("errorMessage", "Incorrect username or password");
        }

        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        userService.register(registerRequest);
        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {
        User user = userService.getById(authenticationUserDetails.getUserId());
        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @GetMapping("about")
    public String getAboutPage() {
        return "about";
    }

    @GetMapping("accommodation")
    public ModelAndView getAccommodationPage() {
        ModelAndView modelAndView = new ModelAndView("accommodation");
        modelAndView.addObject("apartments", apartmentService.getApartments());
        return modelAndView;
    }

    @GetMapping("discover-burgas")
    public String getDiscoverBurgas() {
        return "discover-burgas";
    }

    @GetMapping("contact")
    public String getContactPage() {
        return "contact";
    }

    @GetMapping("privacy")
    public String getPrivacyPage() {
        return "privacy";
    }

    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }
}
