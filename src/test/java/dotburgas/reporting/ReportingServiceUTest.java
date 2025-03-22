package dotburgas.reporting;

import dotburgas.apartment.model.Apartment;
import dotburgas.reporting.client.ReportingClient;
import dotburgas.reporting.client.dto.ReservationDetails;
import dotburgas.reporting.client.dto.ReservationResponse;
import dotburgas.reporting.client.dto.ReservationStatsResponse;
import dotburgas.reporting.service.ReportingService;
import dotburgas.reservation.model.ConfirmationStatus;
import dotburgas.reservation.model.PaymentStatus;
import dotburgas.reservation.model.Reservation;
import dotburgas.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportingServiceUTest {

    @Mock
    private ReportingClient reportingClient;

    @InjectMocks
    private ReportingService reportingService;

    // 01. getReservationDetails
    @Test
    void givenReservationId_thenReturnReservationResponse() {

        // Given
        UUID reservationId = UUID.randomUUID();
        ReservationResponse reservationResponse = ReservationResponse.builder()
                .reservationId(reservationId)
                .checkInDate(LocalDate.of(2025, 5, 1))
                .checkOutDate(LocalDate.of(2025, 5, 4))
                .guests(3)
                .reservationLength(3)
                .totalPrice(BigDecimal.valueOf(300))
                .user("Petar123")
                .apartment("Apartment 1")
                .build();

        when(reportingClient.getReservationDetails(String.valueOf(reservationId))).thenReturn(ResponseEntity.ok(reservationResponse));

        // When
        ReservationResponse returnedReservationDetails = reportingService.getReservationDetails(reservationId);

        // Then
        assertEquals(reservationResponse.getReservationId(), returnedReservationDetails.getReservationId());
        assertEquals(reservationResponse.getCheckInDate(), returnedReservationDetails.getCheckInDate());
        assertEquals(reservationResponse.getCheckOutDate(), returnedReservationDetails.getCheckOutDate());
        assertEquals(reservationResponse.getGuests(), returnedReservationDetails.getGuests());
        assertEquals(reservationResponse.getReservationLength(), returnedReservationDetails.getReservationLength());
        assertEquals(reservationResponse.getTotalPrice(), returnedReservationDetails.getTotalPrice());
        assertEquals(reservationResponse.getUser(), returnedReservationDetails.getUser());
        assertEquals(reservationResponse.getApartment(), returnedReservationDetails.getApartment());
        verify(reportingClient, times(1)).getReservationDetails(String.valueOf(reservationId));
    }

    // 02. getSummaryStatsPerApartment
    @Test
    void givenNoParameters_thenReturnListOfReservationStatsResponse() {

        // Given

        ReservationStatsResponse reservationStatsResponse1 = ReservationStatsResponse.builder()
                .apartment("Apartment 1")
                .totalRevenue("300")
                .totalBookedDays("50")
                .totalGuestsVisited("40")
                .build();

        ReservationStatsResponse reservationStatsResponse2 = ReservationStatsResponse.builder()
                .apartment("Apartment 2")
                .totalRevenue("350")
                .totalBookedDays("30")
                .totalGuestsVisited("32")
                .build();

        ResponseEntity<List<ReservationStatsResponse>> httpResponse = ResponseEntity.ok((List.of(reservationStatsResponse1, reservationStatsResponse2)));

        when(reportingClient.getSummaryStatsPerApartment()).thenReturn(httpResponse);

        // When
        List<ReservationStatsResponse> returnedSummaryStatsPerApartment = reportingService.getSummaryStatsPerApartment();

        // Then
        assertNotNull(returnedSummaryStatsPerApartment);
        assertEquals(2, returnedSummaryStatsPerApartment.size());
        assertEquals("Apartment 1", returnedSummaryStatsPerApartment.get(0).getApartment());
        assertEquals("300", returnedSummaryStatsPerApartment.get(0).getTotalRevenue());
        assertEquals("Apartment 2", returnedSummaryStatsPerApartment.get(1).getApartment());
        assertEquals("350", returnedSummaryStatsPerApartment.get(1).getTotalRevenue());
        assertEquals("30", returnedSummaryStatsPerApartment.get(1).getTotalBookedDays());
        assertEquals("32", returnedSummaryStatsPerApartment.get(1).getTotalGuestsVisited());
        verify(reportingClient, times(1)).getSummaryStatsPerApartment();
    }

    // 03. getReservationHistory
    @Test
    void givenNoParameters_thenReturnListOfReservationResponse() {

        // Given
        ReservationResponse reservationResponse = ReservationResponse.builder()
                .reservationId(UUID.randomUUID())
                .checkInDate(LocalDate.of(2025, 5, 1))
                .checkOutDate(LocalDate.of(2025, 5, 4))
                .guests(3)
                .reservationLength(3)
                .totalPrice(BigDecimal.valueOf(300))
                .user("Petar123")
                .apartment("Apartment 1")
                .build();

        ReservationResponse reservationResponse2 = ReservationResponse.builder()
                .reservationId(UUID.randomUUID())
                .checkInDate(LocalDate.of(2025, 5, 1))
                .checkOutDate(LocalDate.of(2025, 5, 3))
                .guests(4)
                .reservationLength(2)
                .totalPrice(BigDecimal.valueOf(200))
                .user("RadinaMateva")
                .apartment("Apartment 2")
                .build();


        ResponseEntity<List<ReservationResponse>> listOfResponseEntities = ResponseEntity.ok(List.of(reservationResponse, reservationResponse2));
        when(reportingClient.getReservationHistory()).thenReturn(listOfResponseEntities);

        // When
        List<ReservationResponse> returnedListOfReservationResponse = reportingService.getReservationHistory();

        // Then
        assertNotNull(returnedListOfReservationResponse);
        assertEquals(3, returnedListOfReservationResponse.getFirst().getGuests());
        assertEquals("Apartment 2", returnedListOfReservationResponse.get(1).getApartment());
        verify(reportingClient, times(1)).getReservationHistory();
    }

    // 04. saveReservationDetails
    @Test
    void givenValidReservation_thenSaveReservationInTheDatabase() {

        // Given
        User user = User.builder()
                .firstName("Petar")
                .lastName("Matev")
                .email("petar_matev@yahoo.co.uk")
                .build();

        Apartment apartment = Apartment.builder()
                .name("Apartment 1")
                .pricePerNight(BigDecimal.valueOf(100))
                .build();

        BigDecimal pricerPerNight = apartment.getPricePerNight();
        BigDecimal totalReservationPrice = pricerPerNight.add(BigDecimal.valueOf(40)).multiply(BigDecimal.valueOf(3));

        LocalDate checkInDate = LocalDate.now();
        LocalDate checkOutDate = checkInDate.plusDays(3);
        Period period = Period.between(checkInDate, checkOutDate);
        int lengthOfStay = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        Reservation expectedReservation = Reservation.builder()
                .Id(UUID.randomUUID())
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

        ReservationDetails expectedReservationDetails = ReservationDetails.builder()
                .reservationId(UUID.randomUUID())
                .reservationId(expectedReservation.getId())
                .checkInDate(expectedReservation.getCheckInDate())
                .checkOutDate(expectedReservation.getCheckOutDate())
                .guests(expectedReservation.getGuests())
                .reservationLength(expectedReservation.getReservationLength())
                .totalPrice(expectedReservation.getTotalPrice())
                .user(expectedReservation.getUser().getFirstName() + " " + expectedReservation.getUser().getLastName())
                .apartment(expectedReservation.getApartment().getName())
                .build();

        ResponseEntity<Void> httpResponse = ResponseEntity.ok().build();
        when(reportingClient.recordReservationDetails(any(ReservationDetails.class))).thenReturn(httpResponse);

        // When
        reportingService.saveReservationDetails(expectedReservation);


        // Then
        verify(reportingClient, times(1)).recordReservationDetails(argThat(reservationDetails ->
                reservationDetails.getReservationId().equals(expectedReservationDetails.getReservationId()) &&
                        reservationDetails.getCheckInDate().equals(expectedReservationDetails.getCheckInDate()) &&
                        reservationDetails.getCheckOutDate().equals(expectedReservationDetails.getCheckOutDate()) &&
                        reservationDetails.getTotalPrice().compareTo(expectedReservationDetails.getTotalPrice()) == 0 &&
                        reservationDetails.getUser().equals(expectedReservationDetails.getUser()) &&
                        reservationDetails.getApartment().equals(expectedReservationDetails.getApartment()) &&
                        reservationDetails.getGuests() == expectedReservationDetails.getGuests() &&
                        reservationDetails.getReservationLength() == expectedReservationDetails.getReservationLength()
        ));
    }
}
