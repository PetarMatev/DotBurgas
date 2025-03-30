package dotburgas;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.loyalty.model.LoyaltyTier;
import dotburgas.reporting.client.dto.ReservationResponse;
import dotburgas.reporting.client.dto.ReservationStatsResponse;
import dotburgas.transaction.model.Transaction;
import dotburgas.transaction.model.TransactionStatus;
import dotburgas.transaction.model.TransactionType;
import dotburgas.user.model.Country;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.wallet.model.Wallet;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("User")
                .password("123123")
                .role(UserRole.USER)
                .country(Country.BULGARIA)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.ONE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .owner(user)
                .build();

        Loyalty loyalty = Loyalty.builder()
                .id(UUID.randomUUID())
                .loyaltyTier(LoyaltyTier.LEVEL_01)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .owner(user)
                .build();

        user.setLoyalty(loyalty);
        user.setWallet(wallet);

        return user;
    }

    public static Transaction aRandomTransaction() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("User")
                .password("123123")
                .role(UserRole.USER)
                .country(Country.BULGARIA)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return Transaction.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .sender("SenderName")
                .receiver("Petar123")
                .amount(BigDecimal.TEN)
                .balanceLeft(new BigDecimal("100.00"))
                .currency(Currency.getInstance("USD"))
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCEEDED)
                .description("Funds transfer")
                .createdOn(LocalDateTime.now())
                .build();
    }

    public static ReservationResponse aRandomReservationResponse() {

        return ReservationResponse.builder()
                .reservationId(UUID.randomUUID())
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(3))
                .guests(3)
                .reservationLength(3)
                .totalPrice(BigDecimal.TEN)
                .user("TestUser")
                .apartment("Apartment 1")
                .build();
    }

    public static ReservationStatsResponse aRandomReservationStatsResponse() {

        return ReservationStatsResponse.builder()
                .apartment("Apartment 1")
                .totalRevenue(BigDecimal.valueOf(500).toString())
                .totalBookedDays("50")
                .totalGuestsVisited("35")
                .build();
    }
}
