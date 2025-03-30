package dotburgas.web;

import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.transaction.model.Transaction;
import dotburgas.transaction.model.TransactionStatus;
import dotburgas.transaction.model.TransactionType;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.service.UserService;
import dotburgas.wallet.service.WalletService;
import dotburgas.web.dto.TransferRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

import static dotburgas.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
public class TransferControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private WalletService walletService;

    @Autowired
    private MockMvc mockMvc;

    // 01. getTransferPage
    @Test
    void getTransferPage_shouldReturnTransferView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        User mockUser = aRandomUser();
        aRandomUser().setId(userId);
        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                userId,
                "User123",
                "123123",
                UserRole.USER
        );

        when(userService.getById(userId)).thenReturn(mockUser);

        // 2. Send Request
        mockMvc.perform(get("/transfers")
                        .with(user(principal))) // Add proper authentication
                .andExpect(status().isOk())
                .andExpect(view().name("transfer"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("transferRequest"));
    }

    // 02. initiateTransfer
    @Test
    @WithMockUser
    void initiateTransfer_shouldRedirectOnSuccess() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID fromWalletId = UUID.randomUUID();

        User mockUser = aRandomUser();
        aRandomUser().setId(userId);

        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .owner(mockUser)
                .sender("SenderName")
                .receiver("Petar123")
                .amount(BigDecimal.TEN)
                .balanceLeft(new BigDecimal("100.00"))
                .currency(Currency.getInstance("USD"))
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCEEDED)
                .description("Funds transfer")
                .createdOn(LocalDateTime.now())
                .build();

        TransferRequest request = TransferRequest.builder()
                .fromWalletId(fromWalletId)
                .toUserName("Petar123")
                .amount(BigDecimal.TEN)
                .build();

        when(userService.getById(userId)).thenReturn(mockUser);
        when(walletService.transferFunds(any(User.class), any(TransferRequest.class)))
                .thenReturn(transaction);

        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                userId,
                "User123",
                "123123",
                UserRole.USER
        );

        // 2. Send Request
        mockMvc.perform(post("/transfers")
                        .param("fromWalletId", request.getFromWalletId().toString())
                        .param("toUserName", request.getToUserName())
                        .param("amount", request.getAmount().toString())
                        .with(csrf())
                        .with(user(principal)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions/" + transactionId));
    }

    @Test
    @WithMockUser
    void initiateTransfer_withInvalidAmount_shouldReturnErrors() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        User mockedUser = aRandomUser();
        mockedUser.setId(userId);
        when(userService.getById(userId)).thenReturn(mockedUser);

        // 2. Send Request
        mockMvc.perform(post("/transfers")
                        .param("fromWalletId", UUID.randomUUID().toString())
                        .param("toUserName", "validUser")
                        .param("amount", "-100")
                        .param("description", "Test transfer")
                        .with(csrf())
                        .with(user(new AuthenticationUserDetails(
                                userId,
                                "testuser",
                                "password",
                                UserRole.USER)))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("transfer"))
                .andExpect(model().attributeHasErrors("transferRequest"))
                .andExpect(model().attributeHasFieldErrors("transferRequest", "amount"));
    }
}
