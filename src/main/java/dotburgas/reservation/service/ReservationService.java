package dotburgas.reservation.service;

import dotburgas.apartment.service.ApartmentService;
import dotburgas.reservation.model.ConfirmationStatus;
import dotburgas.reservation.model.PaymentStatus;
import dotburgas.reservation.model.Reservation;
import dotburgas.reservation.repository.ReservationRepository;
import dotburgas.transaction.service.TransactionService;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import dotburgas.web.dto.ReservationRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
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
    private final MailSender mailSender;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, TransactionService transactionService, UserService userService, ApartmentService apartmentService, MailSender mailSender) {
        this.reservationRepository = reservationRepository;
        this.apartmentService = apartmentService;
        this.mailSender = mailSender;
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
                .confirmationStatus(ConfirmationStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        reservationRepository.save(reservation);

        notifyAdminForApproval(reservation);
    }

    @Transactional
    public void updateReservationStatus(UUID reservationId, ConfirmationStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new EntityNotFoundException("Reservation not found"));
        reservation.setConfirmationStatus(status);

        if (status == ConfirmationStatus.REJECTED) {
            reservation.setPaymentStatus(PaymentStatus.VOID);
        }
        reservationRepository.save(reservation);
    }



    private long calculateDaysBetween(LocalDate checkInDate, LocalDate checkOutDate) {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public List<Reservation> getReservationsByUser(User user) {
        return reservationRepository.findByUser(user);
    }

    public List<Reservation> getPendingReservations() {
        return reservationRepository.findByConfirmationStatus(ConfirmationStatus.PENDING);
    }

    private void notifyAdminForApproval(Reservation reservation) {
        // adminNotificationService.sendReservationApprovalRequest(reservation);
    }

    public void sendReservationRequestEmail(User user, UUID apartmentId, ReservationRequest reservationRequest) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(reservationRequest.getEmail());
        message.setSubject("Reservation Request");

        String emailBody = String.format("""
                        Dear Admin,
                        
                        A new reservation request has been submitted by %s %s.
                        
                        Reservation Details:
                        User Email: %s
                        User First Name: %s
                        User Last Name: %s
                        Apartment Name: %s
                        Apartment ID: %s
                        Reservation Dates: %s to %s
                        
                        Please review and process the reservation request.
                        
                        Thank you!
                        """,
                user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getFirstName(), user.getLastName(),
                apartmentService.findApartmentNameByID(apartmentId), apartmentId,
                reservationRequest.getCheckInDate(), reservationRequest.getCheckOutDate()
        );

        message.setText(emailBody);

        try {
            mailSender.send(message);
            log.info("Reservation Details email was successfully processed");
        } catch (Exception e) {
            log.warn("There was an issue sending an email to %s due to %s.".formatted(reservationRequest.getEmail(), e.getMessage()));
        }
    }
}
