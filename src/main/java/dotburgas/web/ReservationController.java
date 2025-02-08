package dotburgas.web;

import dotburgas.reservation.model.Reservation;
import dotburgas.reservation.service.ReservationService;
import dotburgas.shared.security.RequireAdminRole;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
public class ReservationController {

    private final UserService userService;
    private final ReservationService reservationService;

    @Autowired
    public ReservationController(UserService userService, ReservationService reservationService) {
        this.userService = userService;
        this.reservationService = reservationService;
    }

    @RequireAdminRole
    @GetMapping("/reservation-history")
    public ModelAndView getReservationHistoryPage(HttpSession session) {

        List<Reservation> reservations = reservationService.getAllReservations();
        ModelAndView modelAndView = new ModelAndView("reservation-history");
        modelAndView.addObject("reservations", reservations);
        return modelAndView;
    }

    @GetMapping("/user/user-reservation")
    public ModelAndView getUserReservations(HttpSession session) {
        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        List<Reservation> reservations = reservationService.getReservationsByUser(user);

        ModelAndView modelAndView = new ModelAndView("user-reservations");
        modelAndView.addObject("reservations", reservations);

        return modelAndView;
    }


}
