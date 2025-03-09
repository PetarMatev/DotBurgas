package dotburgas.Reporting.Client.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ReservationDetails {

    private int guests;

    private long reservationLength;

}
