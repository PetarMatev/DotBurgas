package dotburgas.web;

import dotburgas.reservation.service.ReservationService;
import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.transaction.service.TransactionService;
import dotburgas.user.model.UserRole;
import dotburgas.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
public class AdminControllerApiTest {

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void putUnauthorisedRequestToSwitchRole_shouldReturn404AndNotFoundView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticationUserDetails principal = new AuthenticationUserDetails(userId, "User123", "123123", UserRole.USER);
        MockHttpServletRequestBuilder request = put("/admin/users/{id}/role", userId)
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is5xxServerError())
                .andExpect(view().name("internal-server-error"));
    }

    @Test
    void putAuthorisedRequestToSwitchRole_shouldRedirectToUsers() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticationUserDetails principal = new AuthenticationUserDetails(userId, "User123", "123123", UserRole.ADMIN);
        MockHttpServletRequestBuilder request = put("/admin/users/{id}/role", userId)
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
        verify(userService, times(1)).switchRole(any());
    }
}
