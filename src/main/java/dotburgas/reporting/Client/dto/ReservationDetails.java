package dotburgas.reporting.Client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationDetails {

    private int guests;

    private long reservationLength;

}
