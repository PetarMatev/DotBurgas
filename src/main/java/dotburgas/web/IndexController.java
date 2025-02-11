package dotburgas.web;

import dotburgas.apartment.service.ApartmentService;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import dotburgas.web.dto.LoginRequest;
import dotburgas.web.dto.RegisterRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
public class IndexController {

    private final UserService userService;
    private final ApartmentService apartmentService;

    public IndexController(UserService userService, ApartmentService apartmentService) {
        this.userService = userService;
        this.apartmentService = apartmentService;
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        // adding an empty DTO and setting up the attribute name.
        modelAndView.addObject("loginRequest", LoginRequest.builder().build());

        return modelAndView;
    }

    // AutoWiring of HttpSession session -> create new session for this request (if there is no session already)
    @PostMapping("/login")
    public String login(@Valid LoginRequest loginRequest, BindingResult bindingResult, HttpSession session) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        User loggedInUser = userService.login(loginRequest);
        session.setAttribute("user_id", loggedInUser.getId());
        session.setAttribute("role", loggedInUser.getRole().toString());

        return "redirect:/home";
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
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
    public ModelAndView getHomePage(HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

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

    @GetMapping("/logout")
    public String getLogoutPage(HttpSession session) {

        session.invalidate();
        return "redirect:/";
    }

}
