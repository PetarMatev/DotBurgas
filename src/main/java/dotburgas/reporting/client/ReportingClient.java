package dotburgas.reporting.client;

import dotburgas.reporting.client.dto.ReservationDetails;
import dotburgas.reporting.client.dto.ReservationResponse;
import dotburgas.reporting.client.dto.ReservationStatsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "reporting-svc", url = "http://localhost:8082/api/v1/admin/reporting")
public interface ReportingClient {

    @PostMapping("/reservations")
    ResponseEntity<Void> recordReservationDetails(@RequestBody ReservationDetails reservationDetails);

    @GetMapping
    ResponseEntity<List<ReservationResponse>> getReservationHistory();

    @GetMapping("/stats")
    ResponseEntity<List<ReservationStatsResponse>> getSummaryStatsPerApartment();

    @GetMapping("/query")
    ResponseEntity<ReservationResponse> getReservationDetails(@RequestParam("reservationId") String reservationId);
}
