package dotburgas.reservation;

import dotburgas.apartment.model.Apartment;
import dotburgas.apartment.service.ApartmentService;
import dotburgas.reporting.service.ReportingService;
import dotburgas.reservation.model.ConfirmationStatus;
import dotburgas.reservation.model.PaymentStatus;
import dotburgas.reservation.model.Reservation;
import dotburgas.reservation.repository.ReservationRepository;
import dotburgas.reservation.service.ReservationService;
import dotburgas.user.model.User;
import dotburgas.wallet.service.WalletService;
import dotburgas.web.dto.ReservationRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceUTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ApartmentService apartmentService;
    @Mock
    private MailSender mailSender;
    @Mock
    private WalletService walletService;
    @Mock
    private ReportingService reportingService;

    @InjectMocks
    private ReservationService reservationService;

    // 01. getAllReservationsByApartment
    @Test
    void givenApartmentIdThatIsInTheDatabase_thenReturnListOfReservations() {

        // Given
        UUID apartmentId = UUID.randomUUID();
        List<Reservation> reservationList = List.of(Reservation.builder().build(), Reservation.builder().build());

        when(reservationRepository.findByApartmentId(apartmentId)).thenReturn(reservationList);

        // When
        List<Reservation> returnedListOfReservations = reservationService.getAllReservationsByApartment(apartmentId);

        assertEquals(reservationList.size(), returnedListOfReservations.size());
        verify(reservationRepository, times(1)).findByApartmentId(apartmentId);
    }

    // 02. getAllReservations
    @Test
    void givenNoParameters_then_returnAllReservationsInList() {

        List<Reservation> reservationList = List.of(Reservation.builder().build(), Reservation.builder().build());

        when(reservationRepository.findAll()).thenReturn(reservationList);

        // When
        List<Reservation> returnedListOfReservations = reservationService.getAllReservations();

        assertEquals(reservationList, returnedListOfReservations);
        assertEquals(reservationList.size(), returnedListOfReservations.size());
        verify(reservationRepository, times(1)).findAll();
    }

    // 03. getReservationsByUser
    @Test
    void givenUserThatExistInTheDatabase_thenReturnListOfReservations() {

        // Given
        User user = User.builder().build();
        List<Reservation> reservationList = List.of(Reservation.builder().build(), Reservation.builder().build());

        when(reservationRepository.findByUser(user)).thenReturn(reservationList);

        // When
        List<Reservation> returnedListOfReservations = reservationService.getReservationsByUser(user);

        // Then
        assertEquals(reservationList, returnedListOfReservations);
        assertEquals(reservationList.size(), returnedListOfReservations.size());
        verify(reservationRepository, times(1)).findByUser(user);
    }

    // 04. getPendingReservations
    @Test
    void givenNoParameters_thenReturnListOfPendingReservations() {

        // Given
        ConfirmationStatus pendingStatus = ConfirmationStatus.PENDING;

        Reservation reservation1 = Reservation.builder().confirmationStatus(ConfirmationStatus.PENDING).build();
        Reservation reservation2 = Reservation.builder().confirmationStatus(ConfirmationStatus.CONFIRMED).build();
        Reservation reservation3 = Reservation.builder().confirmationStatus(ConfirmationStatus.PENDING).build();

        List<Reservation> reservationList = List.of(reservation1, reservation3);
        when(reservationRepository.findByConfirmationStatus(ConfirmationStatus.PENDING)).thenReturn(reservationList);
        // When
        List<Reservation> returnedPendingReservations = reservationService.getPendingReservations();

        // Then
        assertEquals(2, returnedPendingReservations.size());
        assertEquals(reservation1.getConfirmationStatus(), returnedPendingReservations.getFirst().getConfirmationStatus());
        verify(reservationRepository, times(1)).findByConfirmationStatus(pendingStatus);
    }

    // 05. createReservation
    @Test
    void givenRequiredReservationFields_thenCreateNewReservation() {

        // Given
        UUID apartmentId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).build();

        Apartment apartment = Apartment.builder()
                .id(apartmentId)
                .pricePerNight(BigDecimal.valueOf(100))
                .build();

        BigDecimal pricerPerNight = apartment.getPricePerNight();
        BigDecimal totalReservationPrice = pricerPerNight.add(BigDecimal.valueOf(40)).multiply(BigDecimal.valueOf(3));

        LocalDate checkInDate = LocalDate.now();
        LocalDate checkOutDate = checkInDate.plusDays(3);
        Period period = Period.between(checkInDate, checkOutDate);
        int lengthOfStay = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        ReservationRequest reservationRequest = ReservationRequest.builder()
                .firstName("Petar")
                .lastName("Matev")
                .email("petar_matev@yahoo.co.uk")
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .guests(3)
                .build();

        Reservation expectedReservation = Reservation.builder()
                .user(user)
                .apartment(apartment)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .guests(3)
                .reservationLength(lengthOfStay)
                .confirmationStatus(ConfirmationStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .pricerPerNight(pricerPerNight)
                .totalPrice(totalReservationPrice)
                .build();

        expectedReservation.setId(null);

        when(apartmentService.getById(apartmentId)).thenReturn(apartment);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(expectedReservation);

        // When
        reservationService.createReservation(user, apartmentId, reservationRequest, reservationRequest.getFirstName(),
                reservationRequest.getLastName(), reservationRequest.getEmail());

        // Then
        verify(apartmentService, times(1)).getById(apartmentId);
        verify(reservationRepository, times(1))
                .save(argThat(savedReservation ->
                        savedReservation.getUser().equals(user) &&
                                savedReservation.getApartment().equals(apartment) &&
                                savedReservation.getCheckInDate().equals(checkInDate) &&
                                savedReservation.getCheckOutDate().equals(checkOutDate) &&
                                savedReservation.getGuests() == 3 &&
                                savedReservation.getReservationLength() == lengthOfStay &&
                                savedReservation.getConfirmationStatus() == ConfirmationStatus.PENDING &&
                                savedReservation.getPaymentStatus() == PaymentStatus.PENDING &&
                                savedReservation.getPricerPerNight().compareTo(pricerPerNight) == 0 &&
                                savedReservation.getTotalPrice().compareTo(totalReservationPrice) == 0
                ));

    }

    // 06. updateReservationStatus
    @Test
    void givenReservationIdThatIsNotInTheDatabase_thenThrowException() {

        UUID reservationId = UUID.randomUUID();
        ConfirmationStatus confirmationStatus = ConfirmationStatus.PENDING;
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> reservationService.updateReservationStatus(reservationId, confirmationStatus));
        verify(reservationRepository, times(1)).findById(reservationId);
    }

    @Test
    void givenReservationIdThatIsInTheDatabaseAndConfirmationStatus_thenUpdateReservationStatus() {

        // Given
        UUID reservationId = UUID.randomUUID();
        ConfirmationStatus confirmationStatus = ConfirmationStatus.REJECTED;
        Reservation reservation = Reservation.builder().build();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // When & Then
        reservationService.updateReservationStatus(reservationId, confirmationStatus);
    }

    // 07. sendReservationRequestEmail
}


