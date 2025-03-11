package dotburgas.reporting.client.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ReservationDetails {

    private int guests;

    private long reservationLength;

    private BigDecimal totalPrice;

}
