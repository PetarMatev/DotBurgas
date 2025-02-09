package dotburgas.web;

import dotburgas.apartment.service.ApartmentService;
import dotburgas.reservation.service.ReservationService;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import dotburgas.web.dto.ReservationRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
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

    @GetMapping("accommodation/reservation-request")
    public ModelAndView getReservationForm(@RequestParam UUID apartmentId, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("reservation-request");

        UUID user_id = (UUID) session.getAttribute("user_id");
        User user = userService.getById(user_id);

        String apartmentName = apartmentService.findApartmentNameByID(apartmentId);

        modelAndView.addObject("first_name", user.getFirstName());
        modelAndView.addObject("last_name", user.getLastName());
        modelAndView.addObject("email", user.getEmail());
        modelAndView.addObject("apartmentName", apartmentName);
        modelAndView.addObject("apartmentId", apartmentId);
        modelAndView.addObject("reservationRequest", new ReservationRequest());

        return modelAndView;
    }


    @PostMapping("accommodation/reservation-request")
    public ModelAndView submitReservationRequest(@Valid ReservationRequest reservationRequest, BindingResult bindingResult, @RequestParam UUID apartmentId,
                                                 HttpSession session) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("reservation-request");
            modelAndView.addObject("errors", bindingResult.getAllErrors());
            return modelAndView;
        }

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        reservationService.createReservation(user, apartmentId, reservationRequest);

        return new ModelAndView("redirect:/user/user-reservation");
    }

}
