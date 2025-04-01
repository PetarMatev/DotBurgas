package dotburgas.web;

import dotburgas.reservation.model.ConfirmationStatus;
import dotburgas.reservation.model.Reservation;
import dotburgas.reservation.service.ReservationService;
import dotburgas.transaction.service.TransactionService;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ReservationService reservationService;
    private final UserService userService;


    @Autowired
    public AdminController(ReservationService reservationService, TransactionService transactionService, UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }

    // hasAnyRole - checking any of the given roles.
    // hasRole - we are checking for one particular role
    // hasAuthority - we are checking if in the collection of permission/roles has permission

    @GetMapping("/reservations/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView showPendingReservations() {

        List<Reservation> pendingReservations = reservationService.getPendingReservations();

        ModelAndView modelAndView = new ModelAndView("pending-reservations");
        modelAndView.addObject("pendingReservations", pendingReservations);
        return modelAndView;
    }

    @PostMapping("/reservations/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveReservation(@RequestParam UUID reservationId) {
        reservationService.updateReservationStatus(reservationId, ConfirmationStatus.CONFIRMED);
        return "redirect:/admin/reservations/pending";
    }

    @PostMapping("/reservations/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectReservation(@RequestParam UUID reservationId) {
        reservationService.updateReservationStatus(reservationId, ConfirmationStatus.REJECTED);
        return "redirect:/admin/reservations/pending";
    }

    @GetMapping("/reservation-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getReservationHistoryPage() {
        List<Reservation> reservations = reservationService.getAllReservations();
        ModelAndView modelAndView = new ModelAndView("reservation-history");
        modelAndView.addObject("reservations", reservations);
        return modelAndView;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getAllUsers() {
        List<User> users = userService.getAllUsers();
        ModelAndView modelAndView = new ModelAndView("users");
        modelAndView.addObject("users", users);
        return modelAndView;
    }

    @PutMapping("/users/{id}/role") // PUT /admin/users/{id}/role
    @PreAuthorize("hasRole('ADMIN')")
    public String switchUserRole(@PathVariable UUID id) {
        userService.switchRole(id);
        return "redirect:/admin/users";
    }
}
