package dotburgas.reporting.client;

import dotburgas.reporting.client.dto.ReservationDetails;
import dotburgas.reservation.model.Reservation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "reporting-svc", url = "http://localhost:8082/api/v1/admin/reporting")
public interface ReportingClient {

    @PostMapping("/reservations")
    ResponseEntity<Void> recordReservationDetails(@RequestBody ReservationDetails reservationDetails);

    @GetMapping
    ResponseEntity<List<Reservation>> getReservationHistory();
}
