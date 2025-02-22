package dotburgas.loyalty.service;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.loyalty.model.LoyaltyTier;
import dotburgas.loyalty.repository.LoyaltyRepository;
import dotburgas.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class LoyaltyService {

    private final LoyaltyRepository loyaltyRepository;

    @Autowired
    public LoyaltyService(LoyaltyRepository loyaltyRepository) {
        this.loyaltyRepository = loyaltyRepository;
    }

    public Loyalty createDefaultLoyaltyProgram(User user) {
        Loyalty loyalty = loyaltyRepository.save(initializeLoyaltyProgram(user));
        log.info("Successfully created new LoyaltyProgram with id [%s] and loyaltyTier [%s]".formatted(loyalty.getId(), loyalty.getLoyaltyTier()));

        return loyalty;
    }


    private Loyalty initializeLoyaltyProgram(User user) {

        LocalDateTime now = LocalDateTime.now();

        return Loyalty.builder()
                .owner(user)
                .loyaltyTier(LoyaltyTier.LEVEL_01)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    public void updatedLoyaltySubscription(UUID loyaltyIdOfUser, LoyaltyTier updatedLoyaltyTier) {
        Loyalty loyalty = loyaltyRepository.findById(loyaltyIdOfUser).orElseThrow(
                () -> new IllegalArgumentException("Loyalty not found for ID: " + loyaltyIdOfUser));

        loyalty.setLoyaltyTier(updatedLoyaltyTier);
        loyalty.setUpdatedOn(LocalDateTime.now());
        loyaltyRepository.save(loyalty);

        log.info("Successfully updated LoyaltySubscription with id [%s] to loyaltyTier [%s]".formatted(loyalty.getId(), loyalty.getLoyaltyTier()));
    }
}

