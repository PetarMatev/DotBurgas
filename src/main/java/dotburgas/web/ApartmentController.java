package dotburgas.web;

import dotburgas.apartment.service.ApartmentService;
import dotburgas.reservation.service.ReservationService;
import dotburgas.shared.security.AuthenticationDetails;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import dotburgas.web.dto.ReservationRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("accommodation")
public class ApartmentController {

    private final UserService userService;
    private final ApartmentService apartmentService;
    private final ReservationService reservationService;

    @Autowired
    public ApartmentController(UserService userService, ApartmentService apartmentService, ReservationService reservationService) {
        this.userService = userService;
        this.apartmentService = apartmentService;
        this.reservationService = reservationService;
    }

    @GetMapping("/reservation-request")
    public ModelAndView getReservationForm(@RequestParam UUID apartmentId, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        ModelAndView modelAndView = new ModelAndView("reservation-request");

        User user = userService.getById(authenticationDetails.getUserId());

        String apartmentName = apartmentService.findApartmentNameByID(apartmentId);
        modelAndView.addObject("first_name", user.getFirstName());
        modelAndView.addObject("last_name", user.getLastName());
        modelAndView.addObject("email", user.getEmail());
        modelAndView.addObject("apartmentName", apartmentName);
        modelAndView.addObject("apartmentId", apartmentId);
        modelAndView.addObject("reservationRequest", new ReservationRequest());
        return modelAndView;
    }


    @PostMapping("/reservation-request")
    public ModelAndView submitReservationRequest(@Valid ReservationRequest reservationRequest, BindingResult bindingResult, @RequestParam UUID apartmentId,
                                                 @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("reservation-request");
            modelAndView.addObject("errors", bindingResult.getAllErrors());
            return modelAndView;
        }

        User user = userService.getById(authenticationDetails.getUserId());

        reservationService.createReservation(user, apartmentId, reservationRequest);

        String emailAddressOfAdmin = "petargmatev@gmail.com";

        String reservationDetailsForAdmin = "Dear Admin,\n\n" +
                "A new reservation request has been submitted by " + user.getFirstName() + " " + user.getLastName() + ".\n\n" +
                "Reservation Details:\n" +
                "User Email: " + user.getEmail() + "\n" +
                "User First Name: " + user.getFirstName() + "\n" +
                "User Last Name: " + user.getLastName() + "\n" +
                "Apartment Name: " + apartmentService.findApartmentNameByID(apartmentId) + "\n" +
                "Apartment ID: " + apartmentId + "\n" +
                "Reservation Dates: " + reservationRequest.getCheckInDate() + " to " + reservationRequest.getCheckOutDate() + "\n" +
                "Please review and process the reservation request.\n\n" +
                "Thank you!";

        userService.sendReservationRequestEmail(emailAddressOfAdmin, reservationDetailsForAdmin);

        return new ModelAndView("redirect:/users/user-reservations");
    }
}
