package dotburgas.web;

import dotburgas.apartment.service.ApartmentService;
import dotburgas.notification.client.dto.Notification;
import dotburgas.notification.client.dto.NotificationPreference;
import dotburgas.notification.service.NotificationService;
import dotburgas.reservation.service.ReservationService;
import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static dotburgas.TestBuilder.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
public class NotificationControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private  ApartmentService apartmentService;

    @MockitoBean
    private  ReservationService reservationService;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    // 1. getNotificationPage
    @Test
    void getNotificationPage_shouldReturnModelWithAllAttributes() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                userId,
                "User123",
                "123123",
                UserRole.USER
        );

        User mockUser = aRandomUser();
        mockUser.setId(userId);
        when(userService.getById(userId)).thenReturn(mockUser);

        // Mock notification preference
        NotificationPreference mockPreference = aRandomNotificationPreference();
        when(notificationService.getNotificationPreference(userId)).thenReturn(mockPreference);

         Notification success1 = aRandomnotification();
        success1.setStatus("SUCCEEDED");

        Notification success2 = aRandomnotification();
        success2.setStatus("SUCCEEDED");

        Notification failed1 = aRandomnotification();
        failed1.setStatus("FAILED");

        Notification pending = aRandomnotification();
        pending.setStatus("PENDING");

        List<Notification> mockHistory = List.of(success1, success2, failed1, pending);
        when(notificationService.getNotificationHistory(userId)).thenReturn(mockHistory);

        // 2. Send Request
        mockMvc.perform(get("/notifications")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("notificationPreference"))
                .andExpect(model().attributeExists("notificationHistory"))
                .andExpect(model().attribute("succeededNotificationsNumber", 2L)) // 2 SUCCEEDED
                .andExpect(model().attribute("failedNotificationsNumber", 1L))   // 1 FAILED
                .andExpect(model().attribute("notificationHistory", hasSize(4))); // All 4 notifications
        verify(userService).getById(userId);
        verify(notificationService).getNotificationPreference(userId);
        verify(notificationService).getNotificationHistory(userId);
    }

    // 2. updateUserPreference
    @Test
    void putUpdateUserPreference_shouldReturnNotificationsView() throws Exception {
        // 1. Build Request
        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                UUID.randomUUID(),
                "User123",
                "123123",
                UserRole.USER
        );

        UUID userId = principal.getUserId();
        boolean enabled = true;
        doNothing().when(notificationService).updateNotificationPreference(userId, enabled);

        // 2. Send Request
        mockMvc.perform(put("/notifications/user-preference")
                        .param("enabled", String.valueOf(enabled))
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(notificationService).updateNotificationPreference(userId, enabled);
    }

    // 3. deleteNotificationHistory
    @Test
    void deleteRequestToDeleteNotificationHistory_shouldReturnRedirectToNotificationsView() throws Exception {

        // 1. Build Request
        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                UUID.randomUUID(),
                "User123",
                "123123",
                UserRole.USER
        );

        UUID userId = principal.getUserId();
        doNothing().when(notificationService).clearHistory(userId);

        // 2. Send Request
        mockMvc.perform(delete("/notifications")
                        .with(user(principal))
                        .with(csrf())) //
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(notificationService).clearHistory(userId);
    }

    // 4. retryFailedNotifications
    @Test
    void retryFailedNotifications_shouldReturnRedirectToNotificationsView() throws Exception {

        // 1. Build Request
        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                UUID.randomUUID(),
                "User123",
                "123123",
                UserRole.USER
        );

        UUID userId = principal.getUserId();
        doNothing().when(notificationService).retryFailed(userId);

        // 2. Send Request
        mockMvc.perform(put("/notifications")
                        .with(user(principal))
                        .with(csrf())) //
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(notificationService).retryFailed(userId);
    }

}
