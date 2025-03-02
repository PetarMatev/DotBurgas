package dotburgas.web;

import dotburgas.apartment.service.ApartmentService;
import dotburgas.reservation.model.Reservation;
import dotburgas.reservation.service.ReservationService;
import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import dotburgas.web.dto.ReservationRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
public class ReservationController {

    private final UserService userService;
    private final ApartmentService apartmentService;
    private final ReservationService reservationService;

    @Autowired
    public ReservationController(UserService userService, ApartmentService apartmentService, ReservationService reservationService) {
        this.userService = userService;
        this.apartmentService = apartmentService;
        this.reservationService = reservationService;
    }

    @GetMapping("/reservation-request")
    public ModelAndView getReservationForm(@RequestParam UUID apartmentId, @AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {
        ModelAndView modelAndView = new ModelAndView("reservation-request");

        String apartmentName = apartmentService.findApartmentNameByID(apartmentId);
        User user = userService.getById(authenticationUserDetails.getUserId());

        modelAndView.addObject("firstName", user.getFirstName());
        modelAndView.addObject("lastName", user.getLastName());
        modelAndView.addObject("email", user.getEmail());
        modelAndView.addObject("apartmentName", apartmentName);
        modelAndView.addObject("apartmentId", apartmentId);
        modelAndView.addObject("reservationRequest", new ReservationRequest());
        return modelAndView;
    }

    @PostMapping("/reservation-request")
    public ModelAndView submitReservationRequest(@Valid ReservationRequest reservationRequest, BindingResult bindingResult, @RequestParam UUID apartmentId,
                                                 @AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("reservation-request");
            modelAndView.addObject("errors", bindingResult.getAllErrors());
            return modelAndView;
        }

        User user = userService.getById(authenticationUserDetails.getUserId());

        reservationService.createReservation(user, apartmentId, reservationRequest);
        reservationService.sendReservationRequestEmail(user, apartmentId, reservationRequest);

        return new ModelAndView("redirect:/user-reservations");
    }

    @GetMapping("/user-reservations")
    public ModelAndView getUserReservations(@AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {

        User user = userService.getById(authenticationUserDetails.getUserId());

        List<Reservation> reservations = reservationService.getReservationsByUser(user);

        ModelAndView modelAndView = new ModelAndView("user-reservations");
        modelAndView.addObject("reservations", reservations);

        return modelAndView;
    }
}
