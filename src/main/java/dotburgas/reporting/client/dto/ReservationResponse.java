package dotburgas.reporting.client.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ReservationResponse {

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private int guests;

    private long reservationLength;

    private BigDecimal totalPrice;

    private String user;

    private String apartment;
}
