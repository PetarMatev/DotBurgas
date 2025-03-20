package dotburgas.web;

import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.transaction.model.Transaction;
import dotburgas.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ModelAndView getAllTransactions(@AuthenticationPrincipal AuthenticationUserDetails authenticationUserDetails) {

        List<Transaction> transactions = transactionService.getAllByOwnerId(authenticationUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView("transactions");
        modelAndView.addObject("transactions", transactions);

        return modelAndView;
    }

    @GetMapping("/{id}")
    public ModelAndView getTransactionById(@PathVariable UUID id) {
        Transaction transaction = transactionService.getById(id);
        ModelAndView modelAndView = new ModelAndView("transaction-result");
        modelAndView.addObject("transaction", transaction);

        return modelAndView;
    }
}
