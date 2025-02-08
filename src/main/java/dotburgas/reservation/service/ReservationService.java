package dotburgas.reservation.service;

import dotburgas.apartment.service.ApartmentService;
import dotburgas.reservation.model.Reservation;
import dotburgas.reservation.repository.ReservationRepository;
import dotburgas.transaction.service.TransactionService;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import dotburgas.web.dto.ReservationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ApartmentService apartmentService;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, TransactionService transactionService, UserService userService, ApartmentService apartmentService) {
        this.reservationRepository = reservationRepository;
        this.apartmentService = apartmentService;
    }

    public List<Reservation> getAllReservationsByApartment(UUID apartmentId) {
        return reservationRepository.findByApartmentId(apartmentId);
    }


    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public void createReservation(User user, UUID apartmentId, ReservationRequest reservationRequest) {
        Reservation reservation = Reservation.builder()
                .user(user)
                .apartment(apartmentService.getById(apartmentId))
                .checkInDate(reservationRequest.getCheckInDate())
                .checkOutDate(reservationRequest.getCheckOutDate())
                .guests(reservationRequest.getGuests())
                .reservationLength(calculateDaysBetween(reservationRequest.getCheckInDate(), reservationRequest.getCheckOutDate()))
                .build();

        reservationRepository.save(reservation);
    }

    private long calculateDaysBetween(LocalDate checkInDate, LocalDate checkOutDate) {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public List<Reservation> getReservationsByUser(User user) {

        return reservationRepository.findByUser(user);
    }
}
