package dotburgas.reporting.client;

import dotburgas.reporting.client.dto.ReservationDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "reporting-svc", url = "http://localhost:8082/api/v1/reporting")
public interface ReportingClient {

    @PostMapping("/reservations")
    ResponseEntity<Void> recordReservationDetails(@RequestBody ReservationDetails reservationDetails);
}
