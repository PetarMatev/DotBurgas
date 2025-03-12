package dotburgas.reporting.client.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ReservationResponse {

    private UUID reservationId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private int guests;

    private long reservationLength;

    private BigDecimal totalPrice;

    private String user;

    private String apartment;
}
