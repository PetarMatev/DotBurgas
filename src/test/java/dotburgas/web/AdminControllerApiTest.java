package dotburgas.web;

import dotburgas.reservation.model.Reservation;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static dotburgas.TestBuilder.aRandomReservation;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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


    // 01. showPendingReservations
    @Test
    void showPendingReservations_shouldReturnViewWithPendingReservations() throws Exception {

        // 1. Build Request
        UUID apartmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Reservation reservationOne = aRandomReservation();
        Reservation reservationTwo = aRandomReservation();

        List<Reservation> pendingReservations = List.of(reservationOne, reservationTwo);

        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                userId,
                "user123",
                "password123",
                UserRole.ADMIN
        );
        when(reservationService.getPendingReservations()).thenReturn(pendingReservations);

        // 2. Send Request
        mockMvc.perform(get("/admin/reservations/pending")
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("pending-reservations"))
                .andExpect(model().attributeExists("pendingReservations"));
    }


    // 02. approveReservation
    @Test
    void postRequestToApproveReservationWithReservationID_shouldReturnRedirectToPendingReservations() throws Exception {

        // 01. Build Request
        MockHttpServletRequestBuilder request = post("/admin/reservations/approve")
                .param("reservationId", UUID.randomUUID().toString());

        // 02. Send Request and Verify
        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());
    }

    // 02. approveReservation
    @Test
    void postRequestApproveReservationPage_shouldRedirectToPendingReservationsView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        AuthenticationUserDetails principal = new AuthenticationUserDetails(userId, "User123", "123123", UserRole.ADMIN);
        MockHttpServletRequestBuilder request = post("/admin/reservations/approve")
                .param("reservationId", reservationId.toString())
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reservations/pending"));
    }

    // 03. rejectReservation
    @Test
    void postRequestRejectReservationPage_shouldRedirectToPendingReservationsView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        AuthenticationUserDetails principal = new AuthenticationUserDetails(userId, "User123", "123123", UserRole.ADMIN);
        MockHttpServletRequestBuilder request = post("/admin/reservations/reject")
                .param("reservationId", reservationId.toString())
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reservations/pending"));
    }

    // 04. getReservationHistoryPage
    @Test
    void getRequestGetReservationHistoryPage_shouldReturnReservationHistoryView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticationUserDetails principal = new AuthenticationUserDetails(userId, "User123", "123123", UserRole.ADMIN);
        MockHttpServletRequestBuilder request = get("/admin/reservation-history")
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("reservation-history"))
                .andExpect(model().attributeExists("reservations"));
    }

    // 05. getAllUsers
    @Test
    void getRequestGetAllUsers_shouldReturnUsersView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticationUserDetails principal = new AuthenticationUserDetails(userId, "User123", "123123", UserRole.ADMIN);
        MockHttpServletRequestBuilder request = get("/admin/users")
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("users"));
        verify(userService, times(1)).getAllUsers();
    }


    // 06. switchUserRole
    @Test
    void putAuthorizedRequestToSwitchRole_shouldRedirectToUsers() throws Exception {

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

    @Test
    void putUnauthorisedRequestToSwitchRole_shouldReturn404AndNotFoundView() throws Exception {

        // 6. Build Request
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
}
