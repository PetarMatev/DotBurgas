package dotburgas.web;

import dotburgas.apartment.service.ApartmentService;
import dotburgas.reporting.service.ReportingService;
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
    private final ReportingService reportingService;

    @Autowired
    public ReservationController(UserService userService, ApartmentService apartmentService, ReservationService reservationService, ReportingService reportingService) {
        this.userService = userService;
        this.apartmentService = apartmentService;
        this.reservationService = reservationService;
        this.reportingService = reportingService;
    }

    @GetMapping("/reservation-request")
    public ModelAndView getReservationForm(@RequestParam UUID apartmentId, @AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {
        ModelAndView modelAndView = new ModelAndView("reservation-request");

        String apartmentName = apartmentService.findApartmentNameByID(apartmentId);
        User user = userService.getById(authenticationUserDetails.getUserId());

        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setFirstName(user.getFirstName());
        reservationRequest.setLastName(user.getLastName());
        reservationRequest.setEmail(user.getEmail());

        modelAndView.addObject("user", user);
        modelAndView.addObject("apartmentName", apartmentName);
        modelAndView.addObject("apartmentId", apartmentId);
        modelAndView.addObject("reservationRequest", reservationRequest);
        return modelAndView;
    }


    @PostMapping("/reservation-request")
    public ModelAndView submitReservationRequest(@Valid ReservationRequest reservationRequest,
                                                 BindingResult bindingResult,
                                                 @RequestParam UUID apartmentId,
                                                 @RequestParam String firstName,
                                                 @RequestParam String lastName,
                                                 @RequestParam String email,
                                                 @AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("reservation-request");
            modelAndView.addObject("errors", bindingResult.getAllErrors());
            return modelAndView;
        }

        User user = userService.getById(authenticationUserDetails.getUserId());

        reservationService.createReservation(user, apartmentId, reservationRequest, firstName, lastName, email);

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

    @GetMapping("/admin/reporting")
    public ModelAndView getReservationPage(@AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {

        User user = userService.getById(authenticationUserDetails.getUserId());
        List<Reservation> reservationHistory = reportingService.getReservationHistory();

        ModelAndView modelAndView = new ModelAndView("reporting-svc");
        modelAndView.addObject("reservationHistory", reservationHistory);

        return modelAndView;
    }
}
