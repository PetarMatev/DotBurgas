package dotburgas.reporting.client.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class ReservationStatsResponse {

    private String apartment;

    private String totalRevenue;

    private String totalBookedDays;

    private String totalGuestsVisited;
}
