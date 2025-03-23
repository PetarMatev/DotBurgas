package dotburgas.notification.client.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Data
@Getter
@Builder
public class NotificationPreference {

    private boolean enabled;

    private String type;

    private String contactInfo;
}
