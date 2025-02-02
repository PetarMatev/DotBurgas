package dotburgas.web;

import dotburgas.shared.security.SecurityUtils;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import dotburgas.web.dto.UserEditRequest;
import dotburgas.web.mapper.DtoMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{id}/profile")
    public ModelAndView getProfileMenu(@PathVariable UUID id) {

        User user = userService.getById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");
        modelAndView.addObject("user", user);
        modelAndView.addObject("userEditRequest", DtoMapper.mapUserToUserEditRequest(user));

        return modelAndView;
    }

    @PutMapping("/users/{id}/profile")
    public ModelAndView updateUserProfile(@PathVariable UUID id, @Valid UserEditRequest userEditRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            User user = userService.getById(id);
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("profile");
            modelAndView.addObject("user", user);
            modelAndView.addObject("userEditRequest", userEditRequest);
            return modelAndView;
        }

        userService.editUserDetails(id, userEditRequest);
        return new ModelAndView("redirect:/home");
    }

    @GetMapping("/users/calendar")
    public ModelAndView getCalendar() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("calendar");
        return modelAndView;
    }


    @GetMapping("/users")
    public ModelAndView getAllUsers(HttpSession session, RedirectAttributes redirectAttributes) {

        List<User> users = userService.getAllUsers();

        if (SecurityUtils.isAdmin(session)) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("users");
            modelAndView.addObject("users", users);

            return modelAndView;
        }
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "You need to log in as an Admin.");
        return new ModelAndView("redirect:/login");
    }
}
