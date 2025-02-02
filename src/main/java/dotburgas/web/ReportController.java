package dotburgas.web;

import dotburgas.reservation.service.ReservationService;
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

    @GetMapping("/reports")
    public String getReportsPage(HttpSession session, RedirectAttributes redirectAttributes) {

        // Step 1: get the Role from the session
        String userRole = (String) session.getAttribute("role");

        // Step 2: check if the User has Admin role.
        if (userRole != null && userRole.equals("ADMIN")) {
            return "reports";
        }

        // Step 3. Invalidate the session
        session.invalidate();

        // Step 4. Add a flash message
        redirectAttributes.addFlashAttribute("message", "You need to log in as an Admin.");

        // Step 5. Redirect to return page with flash message presented
        return "redirect:/login";
    }
}
