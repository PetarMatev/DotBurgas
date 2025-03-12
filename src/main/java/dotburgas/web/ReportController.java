package dotburgas.web;

import dotburgas.reporting.client.dto.ReservationResponse;
import dotburgas.reporting.client.dto.ReservationStatsResponse;
import dotburgas.reporting.service.ReportingService;
import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/reporting")
public class ReportController {

    private final UserService userService;
    private final ReportingService reportingService;

    @Autowired
    public ReportController(UserService userService, ReportingService reportingService) {
        this.userService = userService;
        this.reportingService = reportingService;
    }

    @GetMapping
    public ModelAndView getReservationPage(@AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {

        User user = userService.getById(authenticationUserDetails.getUserId());
        List<ReservationResponse> reservationHistory = reportingService.getReservationHistory();
        reservationHistory = reservationHistory.stream().limit(6).toList();

        ModelAndView modelAndView = new ModelAndView("reporting-svc");
        modelAndView.addObject("reservationHistory", reservationHistory);

        return modelAndView;
    }

    @GetMapping("/stats")
    public ModelAndView getStatsPage() {

        List<ReservationStatsResponse> reservationStatsResponse = reportingService.getSummaryStatsPerApartment();

        ModelAndView modelAndView = new ModelAndView("analytics");
        modelAndView.addObject("reservationStatsResponse", reservationStatsResponse);

        return modelAndView;
    }

    @GetMapping("/query")
    public ModelAndView retrieveReservationDetails(@RequestParam UUID reservationId) {
        ReservationResponse reservationResponse = reportingService.getReservationDetails(reservationId);

        ModelAndView modelAndView = new ModelAndView("query");
        modelAndView.addObject("reservationResponse", reservationResponse);
        return modelAndView;
    }
}
