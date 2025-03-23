package dotburgas.notification;

import dotburgas.notification.client.NotificationClient;
import dotburgas.notification.client.dto.Notification;
import dotburgas.notification.client.dto.NotificationPreference;
import dotburgas.notification.client.dto.NotificationRequest;
import dotburgas.notification.client.dto.UpsertNotificationPreference;
import dotburgas.notification.service.NotificationService;
import dotburgas.shared.exception.NotificationServiceFeignCallException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUTest {

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private NotificationService notificationService;

    // 01. getNotificationPreference

    @Test
    void givenInvalidUserId_whenGetNotificationPreference_thenThrowException() {

        // Given
        UUID userId = UUID.randomUUID();
        when(notificationClient.getUserPreference(userId)).thenReturn(ResponseEntity.notFound().build());

        // When & Then
        assertThrows(RuntimeException.class, () -> notificationService.getNotificationPreference(userId));
        verify(notificationClient, times(1)).getUserPreference(userId);
    }

    @Test
    void givenUserIdWhichIsValidInTheDatabase_thenReturnNotificationPreference() {
        // Given
        UUID userId = UUID.randomUUID();

        NotificationPreference notificationPreference = NotificationPreference.builder()
                .enabled(true)
                .type("Email")
                .contactInfo("petar_matev@yahoo.co.uk")
                .build();

        when(notificationClient.getUserPreference(userId)).thenReturn(ResponseEntity.ok().body(notificationPreference));

        // When
        NotificationPreference returnedNotificationPreference = notificationService.getNotificationPreference(userId);

        // Then
        assertTrue(returnedNotificationPreference.isEnabled());
        assertEquals("Email", returnedNotificationPreference.getType());
        assertEquals("petar_matev@yahoo.co.uk", returnedNotificationPreference.getContactInfo());
        verify(notificationClient, times(1)).getUserPreference(userId);

    }

    // 02. getNotificationHistory
    @Test
    void givenCorrectUserIdThatExistInTheDatabase_ThenReturnListOfNotifications() {

        // Given
        UUID userId = UUID.randomUUID();

        Notification notification = Notification.builder()
                .subject("Payment")
                .createdOn(LocalDateTime.now())
                .status("Completed")
                .type("Email")
                .build();

        Notification notification2 = Notification.builder()
                .subject("Withdrawal")
                .createdOn(LocalDateTime.now())
                .status("Pending")
                .type("Email")
                .build();

        List<Notification> notificationList = List.of(notification, notification2);

        when(notificationClient.getNotificationHistory(userId)).thenReturn(ResponseEntity.ok().body(notificationList));

        // When
        List<Notification> returnedNotificationHistory = notificationService.getNotificationHistory(userId);

        // Then
        assertEquals(notification.getSubject(), returnedNotificationHistory.getFirst().getSubject());
        assertEquals(notification.getCreatedOn(), returnedNotificationHistory.getFirst().getCreatedOn());
        assertEquals(notification.getStatus(), returnedNotificationHistory.getFirst().getStatus());
        assertEquals(notification.getType(), returnedNotificationHistory.getFirst().getType());
        verify(notificationClient, times(1)).getNotificationHistory(userId);
    }

    // 03. sendNotification
    @Test
    void givenUserIdAndEmailSubjectAndEmailBody_thenSendNotification() throws NoSuchFieldException, IllegalAccessException {

        // Given
        UUID userId = UUID.randomUUID();
        String emailSubject = "new Notification";
        String emailBody = "Body of this Email!";
        String email = "petar_matev@yahoo.co.uk";

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .subject(emailSubject)
                .body(emailBody)
                .build();

        ResponseEntity<Void> httpExpectedResponse = null;

        when(notificationClient.sendNotification(notificationRequest)).thenReturn(httpExpectedResponse);

        // When
        notificationService.sendNotification(userId, emailSubject, emailBody);

        // Then
        verify(notificationClient, times(1)).sendNotification(notificationRequest);
    }

    @Test
    void givenUserIdAndEmailSubjectAndEmailBody_thenLogsErrorWhenResponseIsUnsuccessful() throws IllegalAccessException, NoSuchFieldException {

        // Given
        UUID userId = UUID.randomUUID();
        String emailSubject = "new Notification";
        String emailBody = "Body of this Email!";

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .subject(emailSubject)
                .body(emailBody)
                .build();

        ResponseEntity<Void> httpExpectedResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        when(notificationClient.sendNotification(notificationRequest)).thenReturn(httpExpectedResponse);

        // When
        notificationService.sendNotification(userId, emailSubject, emailBody);

        // Then
        verify(notificationClient, times(1)).sendNotification(notificationRequest);
    }

    @Test
    void givenUserIdAndEmailSubjectAndEmailBody_thenLogsWarningOnException() {
        // Given
        UUID userId = UUID.randomUUID();
        String emailSubject = "new Notification";
        String emailBody = "Body of this Email!";

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .subject(emailSubject)
                .body(emailBody)
                .build();

        when(notificationClient.sendNotification(notificationRequest)).thenThrow(new RuntimeException("Feign error"));

        // When
        notificationService.sendNotification(userId, emailSubject, emailBody);

        // Then
        verify(notificationClient, times(1)).sendNotification(notificationRequest);
    }

    // 04. saveNotificationPreference
    @Test
    void givenUserIdAndBooleanIsEnabledAndEmailAddress_thenSuccessfullyUpsertNotificationPreference() {

        // Given
        UUID userId = UUID.randomUUID();
        boolean isEmailEnabled = true;
        String email = "petar_matev@yahoo.co.uk";

        UpsertNotificationPreference upsertNotificationPreference = UpsertNotificationPreference.builder()
                .userId(userId)
                .contactInfo(email)
                .type("EMAIL")
                .notificationEnabled(isEmailEnabled)
                .build();

        when(notificationClient.upsertNotificationPreference(upsertNotificationPreference)).thenReturn(ResponseEntity.ok().build());

        // When
        notificationService.saveNotificationPreference(userId, isEmailEnabled, email);

        // Then
        verify(notificationClient, times(1)).upsertNotificationPreference(upsertNotificationPreference);
    }

    @Test
    void givenUserIdAndBooleanIsEnabledAndEmailAddress_thenLogsErrorWhenResponseIsUnsuccessful() {

        // Given
        UUID userId = UUID.randomUUID();
        boolean isEmailEnabled = true;
        String email = "petar_matev@yahoo.co.uk";

        UpsertNotificationPreference upsertNotificationPreference = UpsertNotificationPreference.builder()
                .userId(userId)
                .contactInfo(email)
                .type("EMAIL")
                .notificationEnabled(isEmailEnabled)
                .build();

        when(notificationClient.upsertNotificationPreference(upsertNotificationPreference)).thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        // When
        notificationService.saveNotificationPreference(userId, isEmailEnabled, email);

        // Then
        verify(notificationClient, times(1)).upsertNotificationPreference(upsertNotificationPreference);
    }


    // 05. updateNotificationPreference
    @Test
    void givenUserIdAndBooleanEnabled_thenSuccessfullyUpdateNotificationPreferences() {

        // Given
        UUID userId = UUID.randomUUID();
        boolean enabled = true;

        when(notificationClient.updateNotificationPreference(userId, enabled)).thenReturn(ResponseEntity.ok().build());

        // When
        notificationService.updateNotificationPreference(userId, enabled);

        // Then
        verify(notificationClient, times(1)).updateNotificationPreference(userId, enabled);

    }

    @Test
    void givenUserIdAndBooleanEnabled_thenThrowsException() {

        // Given
        UUID userId = UUID.randomUUID();
        boolean enabled = true;
        when(notificationClient.updateNotificationPreference(userId, enabled)).thenThrow(new RuntimeException("Feign error"));

        // When & Then
        assertThrows(NotificationServiceFeignCallException.class, () -> notificationService.updateNotificationPreference(userId, enabled));
    }

    // 06. clearHistory
    @Test
    void givenUserIdThatIsCorrectInTheDatabase_thenSuccessfullyClearHistory() {

        // Given
        UUID userId = UUID.randomUUID();
        when(notificationClient.clearHistory(userId)).thenReturn(ResponseEntity.ok().build());

        // When
        notificationService.clearHistory(userId);

        // Then
        verify(notificationClient, times(1)).clearHistory(userId);
    }

    @Test
    void givenUserIdThatIsCorrectInTheDatabase_thenThrowsException() {

        // Given
        UUID userId = UUID.randomUUID();
        when(notificationClient.clearHistory(userId)).thenThrow(new RuntimeException("Feign error"));

        // When & Then
        assertThrows(NotificationServiceFeignCallException.class, () -> notificationService.clearHistory(userId));
    }


    // 07. retryFailed
    @Test
    void givenUserIdThatIsCorrectInTheDatabase_thenSuccessfullyRetryFailedNotifications() {

        // Given
        UUID userId = UUID.randomUUID();
        when(notificationClient.retryFailedNotifications(userId)).thenReturn(ResponseEntity.ok().build());

        // When
        notificationService.retryFailed(userId);

        // Then
        verify(notificationClient, times(1)).retryFailedNotifications(userId);
    }

    @Test
    void givenUserIdThatIsCorrectInTheDatabase_thenThrows() {

        // Given
        UUID userId = UUID.randomUUID();
        when(notificationClient.retryFailedNotifications(userId)).thenThrow(new RuntimeException("Feign error"));

        // When & Then
        assertThrows(NotificationServiceFeignCallException.class, () -> notificationService.retryFailed(userId));
        verify(notificationClient, times(1)).retryFailedNotifications(userId);
    }
}
