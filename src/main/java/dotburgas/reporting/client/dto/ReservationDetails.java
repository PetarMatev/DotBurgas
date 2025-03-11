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

@Data
@Builder
public class ReservationDetails {

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private int guests;

    private long reservationLength;

    private BigDecimal totalPrice;

    private String user;

    private String apartment;
}
