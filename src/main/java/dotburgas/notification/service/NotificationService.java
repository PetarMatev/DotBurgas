package dotburgas.notification.service;

import dotburgas.notification.client.NotificationClient;
import dotburgas.notification.client.dto.Notification;
import dotburgas.notification.client.dto.NotificationPreference;
import dotburgas.notification.client.dto.NotificationRequest;
import dotburgas.notification.client.dto.UpsertNotificationPreference;
import dotburgas.shared.exception.NotificationServiceFeignCallException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    private final NotificationClient notificationClient;

    @Value("${notification-svc.failure-message.clear-history}")
    private String clearHistoryFailedMessage;

    @Autowired
    public NotificationService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    public NotificationPreference getNotificationPreference(UUID userId) {

        ResponseEntity<NotificationPreference> httpResponse = notificationClient.getUserPreference(userId);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Notification preference for user id [%s] does not exist.".formatted(userId));
        }

        return httpResponse.getBody();
    }

    public List<Notification> getNotificationHistory(UUID userId) {

        ResponseEntity<List<Notification>> httpResponse = notificationClient.getNotificationHistory(userId);

        return httpResponse.getBody();
    }

    public void sendNotification(UUID userId, String emailSubject, String emailBody) {

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .subject(emailSubject)
                .body(emailBody)
                .build();


        // Service to Service Call

        ResponseEntity<Void> httpResponse;

        try {
            httpResponse = notificationClient.sendNotification(notificationRequest);

            if (!httpResponse.getStatusCode().is2xxSuccessful()) {
                log.warn("[Feign call to notification-svc failed] Can't send email to user with id = [%s]".formatted(userId));
            }

        } catch (Exception e) {
            log.warn("Can't send email to user with id = [%s] due to 500 Internal Server Error".formatted(userId));
        }
    }

    public void saveNotificationPreference(UUID userId, boolean isEmailEnabled, String email) {

        UpsertNotificationPreference upsertNotificationPreference = UpsertNotificationPreference.builder()
                .userId(userId)
                .contactInfo(email)
                .type("EMAIL")
                .notificationEnabled(isEmailEnabled)
                .build();


        // Invoke Feign client and execute HTTP post Request.
        ResponseEntity<Void> httpResponse = notificationClient.upsertNotificationPreference(upsertNotificationPreference);
        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            log.error("[Feign call to notification-svc failed] Can't save user preference for user with id = [%s]".formatted(userId));
        }
    }

    public void updateNotificationPreference(UUID userId, boolean enabled) {
        try {
            notificationClient.updateNotificationPreference(userId, enabled);
        } catch (Exception e) {
            log.warn("Can't update notification preferences for user with id = [%s].".formatted(userId));
            throw new NotificationServiceFeignCallException("Failed to update notification preferences for user with ID: " + userId);
        }
    }

    public void clearHistory(UUID userId) {

        try {
            notificationClient.clearHistory(userId);
        } catch (Exception e) {
            log.error("Unable to call notification-svc for clear notification history.");
            throw new NotificationServiceFeignCallException(clearHistoryFailedMessage);
        }
    }

    public void retryFailed(UUID userId) {

        try {
            notificationClient.retryFailedNotifications(userId);
        } catch (Exception e) {
            log.error("Unable to call notification-svc for retry Failed notifications.");
            throw new NotificationServiceFeignCallException(clearHistoryFailedMessage);
        }
    }
}
