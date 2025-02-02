package dotburgas.web.dto;

import dotburgas.loyalty.model.LoyaltyTier;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoyaltySubscriptionEditRequest {

    @Enumerated(EnumType.STRING)
    private LoyaltyTier loyaltyTier;
}
