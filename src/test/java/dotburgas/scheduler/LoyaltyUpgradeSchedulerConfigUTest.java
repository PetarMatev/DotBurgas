package dotburgas.scheduler;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.loyalty.model.LoyaltyTier;
import dotburgas.loyalty.service.LoyaltyService;
import dotburgas.reservation.model.ConfirmationStatus;
import dotburgas.reservation.model.PaymentStatus;
import dotburgas.reservation.model.Reservation;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoyaltyUpgradeSchedulerConfigUTest {

    @Mock
    private UserService userService;
    @Mock
    private LoyaltyService loyaltyService;

    @InjectMocks
    private LoyaltyUpgradeSchedulerConfig loyaltyUpgradeSchedulerConfig;

    @Test
    void givenUserWithReservations_thenReturnConfirmedAndPaidReservations() {
        // Given
        User user = mock(User.class);

        Reservation confirmedAndPaidReservation = Reservation.builder()
                .confirmationStatus(ConfirmationStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PAID)
                .build();

        Reservation confirmedButNotPaidReservation = Reservation.builder()
                .confirmationStatus(ConfirmationStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Reservation notConfirmedReservation = Reservation.builder()
                .confirmationStatus(ConfirmationStatus.PENDING)
                .paymentStatus(PaymentStatus.PAID)
                .build();

        List<Reservation> allReservations = List.of(
                confirmedAndPaidReservation,
                confirmedButNotPaidReservation,
                notConfirmedReservation
        );

        when(user.getReservations()).thenReturn(allReservations);

        // When
        List<Reservation> result = loyaltyUpgradeSchedulerConfig.getConfirmedAndPaidReservations(user);

        // Then
        assertEquals(1, result.size());
        assertEquals(confirmedAndPaidReservation, result.getFirst());
    }

    @Test
    void givenUserWithEnoughReservations_thenUpgradeLoyaltyTier() {
        // Given
        User user = mock(User.class);
        Loyalty loyalty = mock(Loyalty.class);

        when(user.getUsername()).thenReturn("testUser");
        when(user.getLoyalty()).thenReturn(loyalty);
        when(loyalty.getLoyaltyTier()).thenReturn(LoyaltyTier.LEVEL_01);

        Reservation confirmedAndPaidReservation = Reservation.builder()
                .confirmationStatus(ConfirmationStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PAID)
                .build();

        List<Reservation> reservations = List.of(
                confirmedAndPaidReservation,
                confirmedAndPaidReservation,
                confirmedAndPaidReservation,
                confirmedAndPaidReservation,
                confirmedAndPaidReservation
        );

        when(user.getReservations()).thenReturn(reservations);
        when(userService.getAllUsers()).thenReturn(List.of(user));
        when(userService.getUserByUsername("testUser")).thenReturn(user);

        // When
        loyaltyUpgradeSchedulerConfig.loyaltyScannerUpdate();

        // Then
        verify(loyaltyService, times(1))
                .updatedLoyaltySubscription(loyalty.getId(), LoyaltyTier.LEVEL_03);
    }

    @Test
    void givenUserWithNotEnoughReservations_thenDoNotUpgradeLoyaltyTier() {
        // Given
        User user = mock(User.class);
        Loyalty loyalty = mock(Loyalty.class);

        when(user.getUsername()).thenReturn("testUser");
        when(user.getLoyalty()).thenReturn(loyalty);
        when(loyalty.getLoyaltyTier()).thenReturn(LoyaltyTier.LEVEL_01);

        Reservation confirmedAndPaidReservation = Reservation.builder()
                .confirmationStatus(ConfirmationStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PAID)
                .build();

        List<Reservation> reservations = List.of(
                confirmedAndPaidReservation,
                confirmedAndPaidReservation
        );

        when(user.getReservations()).thenReturn(reservations);
        when(userService.getAllUsers()).thenReturn(List.of(user));
        when(userService.getUserByUsername("testUser")).thenReturn(user);

        // When
        loyaltyUpgradeSchedulerConfig.loyaltyScannerUpdate();

        // Then
        verify(loyaltyService, never()).updatedLoyaltySubscription(any(), any());
    }
}
