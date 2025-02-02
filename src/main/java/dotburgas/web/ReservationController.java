package dotburgas.web;

import dotburgas.reservation.model.Reservation;
import dotburgas.reservation.service.ReservationService;
import dotburgas.shared.security.SecurityUtils;
import dotburgas.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ReservationController {

    private final ReservationService reservationService;

    @Autowired
    public ReservationController(UserService userService, ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservation-history")
    public ModelAndView getReservationHistoryPage(HttpSession session, RedirectAttributes redirectAttributes) {

        List<Reservation> reservations = reservationService.getAllReservations();

        if (SecurityUtils.isAdmin(session)) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("reservation-history");
            modelAndView.addObject("reservations", reservations);
            return modelAndView;
        }
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "You need to log in as an Admin.");
        return new ModelAndView("reservation-history");
    }
}
