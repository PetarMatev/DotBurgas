package dotburgas.reservation;

import dotburgas.apartment.model.Apartment;
import dotburgas.apartment.service.ApartmentService;
import dotburgas.reporting.service.ReportingService;
import dotburgas.reservation.model.ConfirmationStatus;
import dotburgas.reservation.model.PaymentStatus;
import dotburgas.reservation.model.Reservation;
import dotburgas.reservation.repository.ReservationRepository;
import dotburgas.reservation.service.ReservationService;
import dotburgas.transaction.model.Transaction;
import dotburgas.transaction.model.TransactionStatus;
import dotburgas.user.model.User;
import dotburgas.wallet.model.Wallet;
import dotburgas.wallet.service.WalletService;
import dotburgas.web.dto.ReservationRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void givenReservationEntity_whenCreatedWithNoArgsConstructor_thenObjectIsInstantiated() {
        // Given
        Reservation reservation = new Reservation();

        // Then
        assertNotNull(reservation);
        assertNull(reservation.getId());
        assertNull(reservation.getCheckInDate());
        assertNull(reservation.getCheckOutDate());
        assertNull(reservation.getConfirmationStatus());
        assertNull(reservation.getPaymentStatus());
    }

    @Test
    void givenReservationEntity_whenFieldsAreSet_thenGettersReturnCorrectValues() {
        // Given
        Reservation reservation = new Reservation();
        UUID id = UUID.randomUUID();
        LocalDate checkInDate = LocalDate.now();
        LocalDate checkOutDate = LocalDate.now().plusDays(3);
        int guests = 2;
        ConfirmationStatus confirmationStatus = ConfirmationStatus.PENDING;
        PaymentStatus paymentStatus = PaymentStatus.PENDING;

        // When
        reservation.setId(id);
        reservation.setCheckInDate(checkInDate);
        reservation.setCheckOutDate(checkOutDate);
        reservation.setGuests(guests);
        reservation.setConfirmationStatus(confirmationStatus);
        reservation.setPaymentStatus(paymentStatus);

        // Then
        assertEquals(id, reservation.getId());
        assertEquals(checkInDate, reservation.getCheckInDate());
        assertEquals(checkOutDate, reservation.getCheckOutDate());
        assertEquals(guests, reservation.getGuests());
        assertEquals(confirmationStatus, reservation.getConfirmationStatus());
        assertEquals(paymentStatus, reservation.getPaymentStatus());
    }

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
    void givenReservationIdThatIsInTheDatabaseAndConfirmationStatus_thenUpdateConfirmationStatusToRejectedAndPaymentStatusToVoid() {
        // Given
        UUID reservationId = UUID.randomUUID();
        ConfirmationStatus confirmationStatus = ConfirmationStatus.REJECTED;
        Reservation reservation = Reservation.builder()
                .confirmationStatus(ConfirmationStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // When
        reservationService.updateReservationStatus(reservationId, confirmationStatus);

        // Then
        assertEquals(confirmationStatus, reservation.getConfirmationStatus());
        assertEquals(PaymentStatus.VOID, reservation.getPaymentStatus());
        verify(reservationRepository, times(1)).save(reservation);
        verifyNoInteractions(walletService, reportingService);
    }

    @Test
    void givenReservationIdThatIsInTheDatabaseAndConfirmationStatus_thenUpdateConfirmationStatusToConfirmedAndPaymentStatusToPaid() {
        // Given
        UUID reservationId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal totalReservationPrice = BigDecimal.valueOf(150.00);

        Wallet wallet = Wallet.builder()
                .id(walletId)
                .balance(BigDecimal.valueOf(1000.00))
                .currency(Currency.getInstance("EUR"))
                .build();

        User user = User.builder()
                .wallet(wallet)
                .build();

        Reservation reservation = Reservation.builder()
                .user(user)
                .totalPrice(totalReservationPrice)
                .confirmationStatus(ConfirmationStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Transaction transaction = Transaction.builder()
                .status(TransactionStatus.SUCCEEDED)
                .build();

        String description = "Payment for reservation Id: %s of EUR %.2f.".formatted(reservationId, totalReservationPrice);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(walletService.charge(user, walletId, totalReservationPrice, description)).thenReturn(transaction);

        // When
        reservationService.updateReservationStatus(reservationId, ConfirmationStatus.CONFIRMED);

        // Then
        assertEquals(ConfirmationStatus.CONFIRMED, reservation.getConfirmationStatus());
        assertEquals(PaymentStatus.PAID, reservation.getPaymentStatus());
        verify(reservationRepository, times(1)).save(reservation);
        verify(walletService, times(1)).charge(user, walletId, totalReservationPrice, description);
        verify(reportingService, times(1)).saveReservationDetails(reservation);
    }

    @Test
    void givenReservationIdThatIsInTheDatabaseAndConfirmationStatusToConfirmedButPaymentFails_thenPaymentStatusShouldNotBePaid() {
        // Given
        UUID reservationId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal totalReservationPrice = BigDecimal.valueOf(150.00);

        Wallet wallet = Wallet.builder()
                .id(walletId)
                .balance(BigDecimal.valueOf(100.00))
                .currency(Currency.getInstance("EUR"))
                .build();

        User user = User.builder()
                .wallet(wallet)
                .build();

        Reservation reservation = Reservation.builder()
                .user(user)
                .totalPrice(totalReservationPrice)
                .confirmationStatus(ConfirmationStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Transaction transaction = Transaction.builder()
                .status(TransactionStatus.FAILED)
                .build();

        String description = "Payment for reservation Id: %s of EUR %.2f.".formatted(reservationId, totalReservationPrice);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(walletService.charge(user, walletId, totalReservationPrice, description)).thenReturn(transaction);

        // When
        reservationService.updateReservationStatus(reservationId, ConfirmationStatus.CONFIRMED);

        // Then
        assertEquals(ConfirmationStatus.CONFIRMED, reservation.getConfirmationStatus());
        assertNotEquals(PaymentStatus.PAID, reservation.getPaymentStatus());
        verify(reservationRepository, times(1)).save(reservation);
        verify(walletService, times(1)).charge(user, walletId, totalReservationPrice, description);
        verifyNoInteractions(reportingService); // No reporting if payment fails
    }

    @Test
    void givenReservationIdThatDoesNotExist_thenThrowEntityNotFoundException() {
        // Given
        UUID reservationId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () ->
                reservationService.updateReservationStatus(reservationId, ConfirmationStatus.CONFIRMED));

        verify(reservationRepository, times(1)).findById(reservationId);
        verifyNoInteractions(walletService, reportingService);
    }

    @Test
    void givenReservationIdThatIsInTheDatabase_whenConfirmationStatusIsNotChanged_thenSaveAndNoFurtherAction() {
        // Given
        UUID reservationId = UUID.randomUUID();
        ConfirmationStatus initialStatus = ConfirmationStatus.PENDING;

        Reservation reservation = Reservation.builder()
                .confirmationStatus(initialStatus)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // When
        reservationService.updateReservationStatus(reservationId, initialStatus);

        // Then
        assertEquals(initialStatus, reservation.getConfirmationStatus());
        verify(reservationRepository, times(1)).save(reservation);
        verifyNoInteractions(walletService, reportingService);
    }

    // 07. sendReservationRequestEmail
    @Test
    void givenInformationDetails_thenSendEmailSuccessfully() {
        // Given
        String adminEmail = "petar_matev@yahoo.co.uk";
        String subject = "Reservation Request";
        UUID apartmentId = UUID.randomUUID();
        String firstName = "Petar";
        String lastName = "Matev";
        String email = "petar_matev@yahoo.co.uk";
        String apartmentName = "Sea View Apartment";

        User user = User.builder().build();

        LocalDate checkInDate = LocalDate.now();
        LocalDate checkOutDate = checkInDate.plusDays(3);

        ReservationRequest reservationRequest = ReservationRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .guests(3)
                .build();

        when(apartmentService.findApartmentNameByID(apartmentId)).thenReturn(apartmentName);

        // When
        reservationService.sendReservationRequestEmail(user, apartmentId, reservationRequest, firstName, lastName, email);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertNotNull(sentMessage);
        assertEquals(adminEmail, sentMessage.getTo()[0]);
        assertEquals(subject, sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(firstName));
        assertTrue(sentMessage.getText().contains(lastName));
        assertTrue(sentMessage.getText().contains(apartmentName));
        assertTrue(sentMessage.getText().contains(checkInDate.toString()));
        assertTrue(sentMessage.getText().contains(checkOutDate.toString()));
    }

    @Test
    void givenInformationDetails_whenEmailFails_thenLogWarning() {
        // Given
        UUID apartmentId = UUID.randomUUID();
        String firstName = "Petar";
        String lastName = "Matev";
        String email = "petar_matev@yahoo.co.uk";
        String apartmentName = "Sea View Apartment";

        User user = User.builder().build();

        LocalDate checkInDate = LocalDate.now();
        LocalDate checkOutDate = checkInDate.plusDays(3);

        ReservationRequest reservationRequest = ReservationRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .guests(3)
                .build();

        when(apartmentService.findApartmentNameByID(apartmentId)).thenReturn(apartmentName);

        doThrow(new MailException("Email error!") {
        }).when(mailSender).send(any(SimpleMailMessage.class));

        // When
        reservationService.sendReservationRequestEmail(user, apartmentId, reservationRequest, firstName, lastName, email);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void givenReservationRequestWithGuestsLessThanOrEqualToTwo_thenCalculateTotalPriceWithoutExtraCharge() {

        // Given
        UUID apartmentId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).build();

        Apartment apartment = Apartment.builder()
                .id(apartmentId)
                .pricePerNight(BigDecimal.valueOf(100))
                .build();

        LocalDate checkInDate = LocalDate.now();
        LocalDate checkOutDate = checkInDate.plusDays(3);
        int lengthOfStay = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        ReservationRequest reservationRequest = ReservationRequest.builder()
                .firstName("Petar")
                .lastName("Matev")
                .email("petar_matev@yahoo.co.uk")
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .guests(2)
                .build();

        BigDecimal expectedTotalPrice = apartment.getPricePerNight().multiply(BigDecimal.valueOf(lengthOfStay));

        when(apartmentService.getById(apartmentId)).thenReturn(apartment);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        reservationService.createReservation(user, apartmentId, reservationRequest, reservationRequest.getFirstName(),
                reservationRequest.getLastName(), reservationRequest.getEmail());

        // Then
        verify(reservationRepository, times(1))
                .save(argThat(savedReservation ->
                        savedReservation.getTotalPrice().compareTo(expectedTotalPrice) == 0
                ));
    }

    @Test
    void givenReservationRequestWithGuestsGreaterThanTwo_thenCalculateTotalPriceWithExtraCharge() {

        // Given
        UUID apartmentId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).build();

        Apartment apartment = Apartment.builder()
                .id(apartmentId)
                .pricePerNight(BigDecimal.valueOf(100))
                .build();

        LocalDate checkInDate = LocalDate.now();
        LocalDate checkOutDate = checkInDate.plusDays(3);
        int lengthOfStay = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        ReservationRequest reservationRequest = ReservationRequest.builder()
                .firstName("Petar")
                .lastName("Matev")
                .email("petar_matev@yahoo.co.uk")
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .guests(3) // Guests > 2
                .build();

        BigDecimal extraCharge = BigDecimal.valueOf(40.00);
        BigDecimal expectedTotalPrice = apartment.getPricePerNight().add(extraCharge).multiply(BigDecimal.valueOf(lengthOfStay));

        when(apartmentService.getById(apartmentId)).thenReturn(apartment);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        reservationService.createReservation(user, apartmentId, reservationRequest, reservationRequest.getFirstName(),
                reservationRequest.getLastName(), reservationRequest.getEmail());

        // Then
        verify(reservationRepository, times(1))
                .save(argThat(savedReservation ->
                        savedReservation.getTotalPrice().compareTo(expectedTotalPrice) == 0
                ));
    }
}


