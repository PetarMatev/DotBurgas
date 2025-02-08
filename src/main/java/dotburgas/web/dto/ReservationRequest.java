package dotburgas.web.dto;

import dotburgas.shared.validation.CheckOutDateAfterCheckInDate;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor  // This generates a no-args constructor
@CheckOutDateAfterCheckInDate  // Your custom validation annotation
public class ReservationRequest {

    @NotNull
    private LocalDate checkInDate;

    @NotNull
    private LocalDate checkOutDate;

    @NotNull
    private int guests;
}