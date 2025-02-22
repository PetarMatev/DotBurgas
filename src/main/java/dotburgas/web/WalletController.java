package dotburgas.web;

import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.transaction.model.Transaction;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import dotburgas.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller

public class WalletController {

    private final UserService userService;
    private final WalletService walletService;

    @Autowired
    public WalletController(UserService userService, WalletService walletService) {
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping("/wallet")
    public ModelAndView getWalletsPage(@AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {

        User user = userService.getById(authenticationUserDetails.getUserId());
        Map<UUID, List<Transaction>> lastFiveTransactionsPerWallet = walletService.getLastFiveTransactions(user.getWallet());

        ModelAndView modelAndView = new ModelAndView("wallet");
        modelAndView.addObject("user", user);
        modelAndView.addObject("lastFiveTransactions", lastFiveTransactionsPerWallet);

        return modelAndView;
    }



}
