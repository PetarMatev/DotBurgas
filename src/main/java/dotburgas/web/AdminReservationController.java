package dotburgas.web;

import dotburgas.reservation.model.ConfirmationStatus;
import dotburgas.reservation.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationController {
    private final ReservationService reservationService;

    @Autowired
    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/pending")
    public ModelAndView showPendingReservations() {
        ModelAndView modelAndView = new ModelAndView("pending-reservations");
        modelAndView.addObject("pendingReservations", reservationService.getPendingReservations());
        return modelAndView;
    }

    @PostMapping("/approve")
    public String approveReservation(@RequestParam UUID reservationId) {
        reservationService.updateReservationStatus(reservationId, ConfirmationStatus.CONFIRMED);
        return "redirect:/admin/reservations/pending";
    }

    @PostMapping("/reject")
    public String rejectReservation(@RequestParam UUID reservationId) {
        reservationService.updateReservationStatus(reservationId, ConfirmationStatus.REJECTED);
        return "redirect:/admin/reservations/pending";
    }
}
