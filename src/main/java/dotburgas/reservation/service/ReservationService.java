package dotburgas.reservation.service;

import dotburgas.reservation.model.Reservation;
import dotburgas.reservation.repository.ReservationRepository;
import dotburgas.transaction.service.TransactionService;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, TransactionService transactionService, UserService userService) {
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> getAllReservationsByApartment(UUID apartmentId) {
        return reservationRepository.findByApartmentId(apartmentId);
    }


    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public void createReservation(UUID apartmentId, LocalDate checkInDate, LocalDate checkOutDate, User user) {

        Reservation reservation = Reservation.builder()
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .user(user)
//                .transaction(transaction)
                .build();
        reservationRepository.save(reservation);
    }
}
