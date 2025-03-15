package dotburgas.loyalty.repository;

import dotburgas.loyalty.model.Loyalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoyaltyRepository extends JpaRepository<Loyalty, UUID> {
}
