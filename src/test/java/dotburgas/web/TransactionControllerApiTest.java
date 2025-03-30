package dotburgas.web;

import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.transaction.model.Transaction;
import dotburgas.transaction.service.TransactionService;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static dotburgas.TestBuilder.aRandomTransaction;
import static dotburgas.TestBuilder.aRandomUser;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(TransactionController.class)
public class TransactionControllerApiTest {

    @MockitoBean
    private  TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    // 1. getAllTransactions
    @Test
    void getAllTransactions_thenReturnTransactionsView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        Transaction TransactionOne = aRandomTransaction();
        Transaction TransactionTwo = aRandomTransaction();
        List<Transaction> transactions = List.of(TransactionOne, TransactionTwo);
        when(transactionService.getAllByOwnerId(userId)).thenReturn(transactions);

        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                userId,
                "User123",
                "123123",
                UserRole.USER
        );

        // 2. Send Request
        mockMvc.perform(get("/transactions")
                        .with(user(principal))) // Add proper authentication
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("transactions"));
    }

    // 2. getTransactionById
    @Test
    void getTransactionById_ThenReturnTransactionResult() throws Exception {

        // 1. Build Request
        UUID transactionId = UUID.randomUUID();
        Transaction TransactionOne = aRandomTransaction();
        when(transactionService.getById(transactionId)).thenReturn(TransactionOne);
        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                UUID.randomUUID(),
                "User123",
                "123123",
                UserRole.USER
        );

        // 2. Send Request
        mockMvc.perform(get("/transactions/{id}", transactionId)
                        .with(user(principal))) // Add proper authentication
                .andExpect(status().isOk())
                .andExpect(view().name("transaction-result"))
                .andExpect(model().attributeExists("transaction"));
    }
}
