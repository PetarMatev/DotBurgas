package dotburgas.reservation.repository;

import dotburgas.reservation.model.ConfirmationStatus;
import dotburgas.reservation.model.Reservation;
import dotburgas.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByApartmentId(UUID apartmentId);

    List<Reservation> findByUser(User user);

    List<Reservation> findByConfirmationStatus(ConfirmationStatus confirmationStatus);
}
