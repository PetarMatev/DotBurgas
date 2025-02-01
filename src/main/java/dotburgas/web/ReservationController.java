package dotburgas.web;

import dotburgas.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReservationController {

    private final UserService userService;

    @Autowired
    public ReservationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/reservation-history")
    public String getReservationHistoryPage() {

        return "reservation-history";
    }

}
