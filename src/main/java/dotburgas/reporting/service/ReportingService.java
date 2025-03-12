package dotburgas.reporting.service;


import dotburgas.reporting.client.ReportingClient;
import dotburgas.reporting.client.dto.ReservationDetails;
import dotburgas.reporting.client.dto.ReservationResponse;
import dotburgas.reporting.client.dto.ReservationStatsResponse;
import dotburgas.reservation.model.Reservation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ReportingService {

    private final ReportingClient reportingClient;

    @Autowired
    public ReportingService(ReportingClient reportingClient) {
        this.reportingClient = reportingClient;
    }

    public void saveReservationDetails(Reservation reservation) {

        ReservationDetails reservationDetails = ReservationDetails.builder()
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .guests(reservation.getGuests())
                .reservationLength(reservation.getReservationLength())
                .totalPrice(reservation.getTotalPrice())
                .user(reservation.getUser().getFirstName() + ' ' + reservation.getUser().getLastName())
                .apartment(reservation.getApartment().getName())
                .build();

        // Invoke Feign Client and execute the HTTP post Request:
        ResponseEntity<Void> httpResponse = reportingClient.recordReservationDetails(reservationDetails);
        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            log.error("[Feign call to reporting-svc failed] Can't save reservation details");
        }
    }

    public List<ReservationResponse> getReservationHistory() {
        ResponseEntity<List<ReservationResponse>> httpResponse = reportingClient.getReservationHistory();
        return httpResponse.getBody();
    }


    public List<ReservationStatsResponse> getSummaryStatsPerApartment() {
        ResponseEntity<List<ReservationStatsResponse>> httpResponse = reportingClient.getSummaryStatsPerApartment();
        return httpResponse.getBody();
    }
}
