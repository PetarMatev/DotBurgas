package dotburgas.web;

import dotburgas.transaction.model.Transaction;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import dotburgas.wallet.service.WalletService;
import dotburgas.web.dto.TransferRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
public class TransferController {

    private final UserService userService;
    private final WalletService walletService;

    public TransferController(UserService userService, WalletService walletService) {
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping("/transfers")
    public ModelAndView getTransferPage(HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transfer");
        modelAndView.addObject("user", user);
        modelAndView.addObject("transferRequest", TransferRequest.builder().build());

        return modelAndView;
    }

    @PostMapping("/transfers")
    public ModelAndView initiateTransfer(@Valid TransferRequest transferRequest, BindingResult bindingResult, HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);


        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("transfer");
            modelAndView.addObject("user", user);
            modelAndView.addObject("transferRequest", transferRequest);

            return modelAndView;
        }

        Transaction transaction = walletService.transferFunds(user, transferRequest);


        return new ModelAndView("redirect:/transactions/" + transaction.getId());
    }
}
