package dotburgas.reporting.client.dto;

import dotburgas.apartment.model.Apartment;
import dotburgas.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ReservationDetails {

    private UUID reservationId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private int guests;

    private long reservationLength;

    private BigDecimal totalPrice;

    private String user;

    private String apartment;
}
