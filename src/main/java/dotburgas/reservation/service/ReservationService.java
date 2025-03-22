package dotburgas.reservation.service;

import dotburgas.apartment.model.Apartment;
import dotburgas.apartment.service.ApartmentService;
import dotburgas.reporting.service.ReportingService;
import dotburgas.reservation.model.ConfirmationStatus;
import dotburgas.reservation.model.PaymentStatus;
import dotburgas.reservation.model.Reservation;
import dotburgas.reservation.repository.ReservationRepository;
import dotburgas.transaction.model.Transaction;
import dotburgas.transaction.model.TransactionStatus;
import dotburgas.user.model.User;
import dotburgas.wallet.service.WalletService;
import dotburgas.web.dto.ReservationRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private final WalletService walletService;
    private final ReportingService reportingService;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, ApartmentService apartmentService, MailSender mailSender, WalletService walletService, ReportingService reportingService) {
        this.reservationRepository = reservationRepository;
        this.apartmentService = apartmentService;
        this.mailSender = mailSender;
        this.walletService = walletService;
        this.reportingService = reportingService;
    }

    public List<Reservation> getAllReservationsByApartment(UUID apartmentId) {
        return reservationRepository.findByApartmentId(apartmentId);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getReservationsByUser(User user) {
        return reservationRepository.findByUser(user);
    }

    public List<Reservation> getPendingReservations() {
        return reservationRepository.findByConfirmationStatus(ConfirmationStatus.PENDING);
    }

    public void createReservation(User user, UUID apartmentId, ReservationRequest reservationRequest, String firstName, String lastName, String email) {

        Apartment apartment = apartmentService.getById(apartmentId);
        long lengthOfStay = calculateDaysBetween(reservationRequest.getCheckInDate(), reservationRequest.getCheckOutDate());
        BigDecimal pricerPerNight = apartment.getPricePerNight();
        BigDecimal totalReservationPrice = calculateReservationPrice(pricerPerNight, reservationRequest.getGuests(), lengthOfStay);

        Reservation reservation = Reservation.builder()
                .user(user)
                .apartment(apartment)
                .checkInDate(reservationRequest.getCheckInDate())
                .checkOutDate(reservationRequest.getCheckOutDate())
                .guests(reservationRequest.getGuests())
                .reservationLength(lengthOfStay)
                .confirmationStatus(ConfirmationStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .pricerPerNight(pricerPerNight)
                .totalPrice(totalReservationPrice)
                .build();

        sendReservationRequestEmail(user, apartmentId, reservationRequest, firstName, lastName, email);
        reservationRepository.save(reservation);
    }

    @Transactional
    public void updateReservationStatus(UUID reservationId, ConfirmationStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new EntityNotFoundException("Reservation not found"));
        reservation.setConfirmationStatus(status);

        if (status == ConfirmationStatus.REJECTED) {
            reservation.setPaymentStatus(PaymentStatus.VOID);
            log.info("Reservation with Id: %s has been %s by the admin.".formatted(reservationId, status));
            reservationRepository.save(reservation);
        } else if (status == ConfirmationStatus.CONFIRMED) {

            // once reservation has been confirmed by the admin, then payment is automatically processed for this reservation
            User user = reservation.getUser();
            UUID walletId = user.getWallet().getId();
            BigDecimal reservationTotalPrice = reservation.getTotalPrice();
            String description = "Payment Reservation with Id: %s for EUR %.2f.".formatted(reservationId, reservation.getTotalPrice());

            Transaction currentTransaction = walletService.charge(user, walletId, reservationTotalPrice, description);

            reservationRepository.save(reservation);
            log.info("Reservation with Id: %s has been %s by the admin.".formatted(reservationId, status));

            // Payment logic after reservation has been authorised from an admin.

            if (!currentTransaction.getStatus().equals(TransactionStatus.FAILED)) {
                // once reservation has been approved by the admin then we can proceed to save the reservation details into reporting-svc.
                reportingService.saveReservationDetails(reservation);

                reservation.setPaymentStatus(PaymentStatus.PAID);
                log.info("Payment of EUR %.2f for reservation with Id: %s has been successfully processed.".formatted(reservationTotalPrice, reservationId));
            } else {
                log.info("Payment of EUR %.2f for reservation with Id: %s has been rejected.".formatted(reservationTotalPrice, reservationId));
            }
        }
    }

    public void sendReservationRequestEmail(User user, UUID apartmentId, ReservationRequest reservationRequest, String firstName, String lastName, String email) {
        SimpleMailMessage message = new SimpleMailMessage();

        String adminEmail = "petar_matev@yahoo.co.uk";
        message.setTo(adminEmail);
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
                firstName, lastName,
                email, firstName, lastName,
                apartmentService.findApartmentNameByID(apartmentId), apartmentId,
                reservationRequest.getCheckInDate(), reservationRequest.getCheckOutDate()
        );

        message.setText(emailBody);

        try {
            mailSender.send(message);
            log.info("Reservation confirmation email sent successfully.");
        } catch (Exception e) {
            log.warn("There was an issue sending an email to %s due to %s.".formatted(reservationRequest.getEmail(), e.getMessage()));
        }
    }

    private BigDecimal calculateReservationPrice(BigDecimal pricePerNight, int guests, long lengthOfStay) {
        BigDecimal totalPrice;
        BigDecimal extraCharge = BigDecimal.valueOf(40.00);

        if (guests <= 2) {
            totalPrice = pricePerNight.multiply(BigDecimal.valueOf(lengthOfStay));
        } else {
            totalPrice = pricePerNight.add(extraCharge).multiply(BigDecimal.valueOf(lengthOfStay));
        }

        return totalPrice;
    }

    private long calculateDaysBetween(LocalDate checkInDate, LocalDate checkOutDate) {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
}
