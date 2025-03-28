package dotburgas;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.loyalty.model.LoyaltyTier;
import dotburgas.user.model.Country;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.wallet.model.Wallet;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
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
}
