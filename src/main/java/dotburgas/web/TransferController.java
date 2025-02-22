package dotburgas.web;

import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.transaction.model.Transaction;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import dotburgas.wallet.service.WalletService;
import dotburgas.web.dto.TransferRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TransferController {

    private final UserService userService;
    private final WalletService walletService;

    @Autowired
    public TransferController(UserService userService, WalletService walletService) {
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping("/transfers")
    public ModelAndView getTransferPage(@AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {

        User user = userService.getById(authenticationUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transfer");
        modelAndView.addObject("user", user);
        modelAndView.addObject("transferRequest", TransferRequest.builder().build());

        return modelAndView;
    }

    @PostMapping("/transfers")
    public ModelAndView initiateTransfer(@Valid TransferRequest transferRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {

        User user = userService.getById(authenticationUserDetails.getUserId());

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
