package dotburgas.loyalty.service;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.loyalty.model.LoyaltyTier;
import dotburgas.loyalty.repository.LoyaltyRepository;
import dotburgas.notification.service.NotificationService;
import dotburgas.user.model.User;
import jakarta.persistence.EntityNotFoundException;
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
    public LoyaltyService(LoyaltyRepository loyaltyRepository, NotificationService notificationService) {
        this.loyaltyRepository = loyaltyRepository;
    }

    public Loyalty createDefaultLoyaltyProgram(User user) {
        Loyalty loyalty = loyaltyRepository.save(initializeLoyaltyProgram(user));
        log.info("Successfully created new LoyaltyProgram with id [%s] and loyaltyTier [%s]".formatted(loyalty.getId(), loyalty.getLoyaltyTier()));
        return loyalty;
    }

    public void updatedLoyaltySubscription(UUID loyaltyId, LoyaltyTier updatedLoyaltyTier) {

        Loyalty loyalty = loyaltyRepository.findById(loyaltyId).orElseThrow(() -> new EntityNotFoundException("Loyalty subscription not found for id: " + loyaltyId));

        loyalty.setLoyaltyTier(updatedLoyaltyTier);
        loyalty.setUpdatedOn(LocalDateTime.now());
        loyaltyRepository.save(loyalty);

        log.info(String.format("Successfully updated LoyaltySubscription with id [%s] to loyaltyTier [%s]",
                loyalty.getId(), loyalty.getLoyaltyTier()));
    }

    private Loyalty initializeLoyaltyProgram(User user) {
        return Loyalty.builder()
                .owner(user)
                .loyaltyTier(LoyaltyTier.LEVEL_01)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }
}

