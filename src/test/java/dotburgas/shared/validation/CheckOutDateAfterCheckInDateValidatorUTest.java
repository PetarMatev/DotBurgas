package dotburgas.shared.validation;

import dotburgas.shared.validation.CheckOutDateAfterCheckInDateValidator;
import dotburgas.web.dto.ReservationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckOutDateAfterCheckInDateValidatorUTest {

    @InjectMocks
    private CheckOutDateAfterCheckInDateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CheckOutDateAfterCheckInDateValidator();
    }

    @Test
    void isValid_ShouldReturnTrue_WhenCheckOutDateIsAfterCheckInDate() {

        // Given
        ReservationRequest reservationRequest = ReservationRequest.builder().build();
        reservationRequest.setCheckInDate(LocalDate.of(2025, 3, 22));
        reservationRequest.setCheckOutDate(LocalDate.of(2025, 3, 23));

        // When
        boolean result = validator.isValid(reservationRequest, mock(ConstraintValidatorContext.class));

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenCheckOutDateIsBeforeCheckInDate() {

        // Given
        ReservationRequest reservationRequest = ReservationRequest.builder().build();
        reservationRequest.setCheckInDate(LocalDate.of(2025, 3, 22));
        reservationRequest.setCheckOutDate(LocalDate.of(2025, 3, 21));

        // When
        boolean result = validator.isValid(reservationRequest, mock(ConstraintValidatorContext.class));

        // Then
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenReservationRequestIsNull() {
        // Given
        ReservationRequest reservationRequest = null;

        // When
        boolean result = validator.isValid(reservationRequest, mock(ConstraintValidatorContext.class));

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenCheckOutDateIsSameAsCheckInDate() {

        // Given
        ReservationRequest reservationRequest = ReservationRequest.builder().build();
        reservationRequest.setCheckInDate(LocalDate.of(2025, 3, 22));
        reservationRequest.setCheckOutDate(LocalDate.of(2025, 3, 22));

        // When
        boolean result = validator.isValid(reservationRequest, mock(ConstraintValidatorContext.class));

        // Then
        assertFalse(result);
    }
}
