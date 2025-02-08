package dotburgas.shared.validation;


import dotburgas.web.dto.ReservationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CheckOutDateAfterCheckInDateValidator implements ConstraintValidator<CheckOutDateAfterCheckInDate, ReservationRequest> {

    @Override
    public boolean isValid(ReservationRequest reservationRequest, ConstraintValidatorContext context) {
        if (reservationRequest == null) {
            return true;
        }

        // Ensure check-out date is after check-in date
        return reservationRequest.getCheckOutDate().isAfter(reservationRequest.getCheckInDate());
    }
}
