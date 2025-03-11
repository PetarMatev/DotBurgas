package dotburgas.reporting.service;


import dotburgas.reporting.client.ReportingClient;
import dotburgas.reporting.client.dto.ReservationDetails;
import dotburgas.reservation.model.Reservation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class ReportingService {

    private final ReportingClient reportingClient;

    @Autowired
    public ReportingService(ReportingClient reportingClient) {
        this.reportingClient = reportingClient;
    }

    public void saveReservationDetails(int guests, long reservationLength, BigDecimal totalPrice) {

        ReservationDetails reservationDetails = ReservationDetails.builder()
                .guests(guests)
                .reservationLength(reservationLength)
                .totalPrice(totalPrice)
                .build();

        // Invoke Feign Client and execute the HTTP post Request:
        ResponseEntity<Void> httpResponse = reportingClient.recordReservationDetails(reservationDetails);
        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            log.error("[Feign call to reporting-svc failed] Can't save reservation details");
        }
    }


    public List<Reservation> getReservationHistory() {
        ResponseEntity<List<Reservation>> httpResponse = reportingClient.getReservationHistory();
        return httpResponse.getBody();
    }
}
