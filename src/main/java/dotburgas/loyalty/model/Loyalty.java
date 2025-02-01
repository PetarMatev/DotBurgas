package dotburgas.loyalty.model;

import dotburgas.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Loyalty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID Id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoyaltyTier loyaltyTier;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;
}
