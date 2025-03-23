package dotburgas.notification;

import dotburgas.notification.client.NotificationClient;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUTest {

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private NotificationService notificationService;

    // 01. getNotificationPreference

    // 02. getNotificationHistory

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

        // Simulate an exception when calling the Feign client
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
