package dotburgas.notification.client;

import dotburgas.notification.client.dto.Notification;
import dotburgas.notification.client.dto.NotificationPreference;
import dotburgas.notification.client.dto.NotificationRequest;
import dotburgas.notification.client.dto.UpsertNotificationPreference;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "notification-svc", url = "http://localhost:8081/api/v1/notifications")
public interface NotificationClient {

    @PostMapping("/preferences")
    ResponseEntity<Void> upsertNotificationPreference(@RequestBody UpsertNotificationPreference upsertNotificationPreference);

    @GetMapping("/preferences")
    ResponseEntity<NotificationPreference> getUserPreference(@RequestParam(name = "userId") UUID userId);

    @PutMapping("/preferences")
    ResponseEntity<Void> updateNotificationPreference(@RequestParam(name="userId") UUID userId, @RequestParam("enabled") boolean enabled);

    @GetMapping
    ResponseEntity<List<Notification>> getNotificationHistory(@RequestParam(name = "userId") UUID userId);

    @PostMapping
    ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest notificationRequest);

    @DeleteMapping
    ResponseEntity<Void> clearHistory(@RequestParam(name="userId") UUID userId);

    @PutMapping
    ResponseEntity<Void> retryFailedNotifications(@RequestParam(name="userId") UUID userId);
}
