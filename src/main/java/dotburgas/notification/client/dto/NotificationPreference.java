package dotburgas.notification.client.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class NotificationPreference {

    private boolean enabled;

    private String type;

    private String contactInfo;
}
