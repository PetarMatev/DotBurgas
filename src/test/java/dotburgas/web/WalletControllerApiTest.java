package dotburgas.web;

import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.transaction.model.Transaction;
import dotburgas.transaction.model.TransactionType;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.service.UserService;
import dotburgas.wallet.model.Wallet;
import dotburgas.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
public class WalletControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private WalletService walletService;

    @Test
    public void getWalletsPage_ShouldReturnWalletViewWithData() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.builder()
                .id(walletId)
                .balance(new BigDecimal("1000.00"))
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .build();

        User mockUser = User.builder()
                .id(userId)
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .wallet(wallet)
                .build();

        Transaction deposit = Transaction.builder()
                .id(UUID.randomUUID())
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("500.00"))
                .description("Top up")
                .createdOn(LocalDateTime.now())
                .build();

        Transaction withdrawal = Transaction.builder()
                .id(UUID.randomUUID())
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("200.00"))
                .description("Payment")
                .createdOn(LocalDateTime.now())
                .build();

        Map<UUID, List<Transaction>> mockTransactions = Map.of(
                walletId, List.of(deposit, withdrawal)
        );

        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                userId, "testuser", "password", UserRole.USER);
        when(userService.getById(userId)).thenReturn(mockUser);
        when(walletService.getLastFiveTransactions(wallet)).thenReturn(mockTransactions);

        // 2. Send Request
        mockMvc.perform(get("/wallet")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("wallet"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("lastFiveTransactions"))
                .andExpect(model().attribute("user", mockUser))
                .andExpect(model().attribute("lastFiveTransactions", mockTransactions));
    }

    @Test
    public void topUpWalletBalance_ShouldRedirectToTransactionPage() throws Exception {

        // 01. Build Request
        UUID walletId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Transaction mockTransaction = Transaction.builder()
                .id(transactionId)
                .amount(BigDecimal.valueOf(500))
                .type(TransactionType.DEPOSIT)
                .description("Wallet top-up")
                .createdOn(LocalDateTime.now())
                .build();

        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                userId, "testuser", "password", UserRole.USER);

        when(walletService.topUp(eq(walletId), eq(BigDecimal.valueOf(500)))).thenReturn(mockTransaction);

        // 2. Send Request
        mockMvc.perform(put("/wallet/{id}/balance/top-up", walletId)
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions/" + transactionId));
        verify(walletService, times(1)).topUp(eq(walletId), eq(BigDecimal.valueOf(500)));
        verifyNoMoreInteractions(walletService);
    }

    @Test
    public void getWalletsPage_Unauthenticated_ShouldRedirectToLogin() throws Exception {

        mockMvc.perform(get("/wallet"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
