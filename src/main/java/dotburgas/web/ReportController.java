package dotburgas.web;

import dotburgas.reservation.service.ReservationService;
import dotburgas.shared.security.RequireAdminRole;
import dotburgas.transaction.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReportController {

    private final TransactionService transactionService;
    private final ReservationService reservationService;

    @Autowired
    public ReportController(TransactionService transactionService, ReservationService reservationService) {
        this.transactionService = transactionService;
        this.reservationService = reservationService;
    }

    @RequireAdminRole
    @GetMapping("/reports")
    public ModelAndView getReportsPage(HttpSession session, RedirectAttributes redirectAttributes) {
            return new ModelAndView("reports");
    }
}
