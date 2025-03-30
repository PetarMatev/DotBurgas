package dotburgas.loyalty.repository;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.loyalty.model.LoyaltyTier;
import dotburgas.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoyaltyRepository extends JpaRepository<Loyalty, UUID> {
    Optional<Loyalty> findByLoyaltyTierAndOwner_Id(LoyaltyTier loyaltyTier, UUID ownerId);
}
