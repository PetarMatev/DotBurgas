package dotburgas.notification.client.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Getter
@Builder

public class Notification {

    private String subject;

    private LocalDateTime createdOn;

    private String status;

    private String type;
}
