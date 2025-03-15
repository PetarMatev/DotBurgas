package dotburgas.scheduler;

import dotburgas.loyalty.model.LoyaltyTier;
import dotburgas.loyalty.service.LoyaltyService;
import dotburgas.reservation.model.ConfirmationStatus;
import dotburgas.reservation.model.PaymentStatus;
import dotburgas.reservation.model.Reservation;
import dotburgas.user.model.User;
import dotburgas.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class LoyaltyUpgradeSchedulerConfig {

    private final UserService userService;
    private final LoyaltyService loyaltyService;

    @Autowired
    public LoyaltyUpgradeSchedulerConfig(UserService userService, LoyaltyService loyaltyService) {
        this.userService = userService;
        this.loyaltyService = loyaltyService;
    }

    @Scheduled(fixedDelay = 180000)
    public void loyaltyScannerUpdate() {

        List<User> users = userService.getAllUsers();

        for (User user : users) {

            // query to get most-up-to-date reservation information for each user.
            User refreshedUser = userService.getUserByUsername(user.getUsername());

            int numberOfReservations = getConfirmedAndPaidReservations(refreshedUser).size();

            if (numberOfReservations > 0) {

                LoyaltyTier loyaltyTier = refreshedUser.getLoyalty().getLoyaltyTier();

                if (numberOfReservations >= 5) {
                    if (!loyaltyTier.equals(LoyaltyTier.LEVEL_03)) {
                        loyaltyService.updatedLoyaltySubscription(refreshedUser.getLoyalty().getId(), LoyaltyTier.LEVEL_03);
                    }

                } else if (numberOfReservations >= 3) {
                    if (!loyaltyTier.equals(LoyaltyTier.LEVEL_02)) {
                        loyaltyService.updatedLoyaltySubscription(refreshedUser.getLoyalty().getId(), LoyaltyTier.LEVEL_02);
                    }
                }
            }
        }
    }

    public List<Reservation> getConfirmedAndPaidReservations(User user) {

        return user
                .getReservations()
                .stream()
                .filter(reservation -> reservation.getConfirmationStatus().equals(ConfirmationStatus.CONFIRMED))
                .filter(reservation -> reservation.getPaymentStatus().equals(PaymentStatus.PAID))
                .toList();
    }
}
