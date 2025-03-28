package dotburgas.user.model;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.reservation.model.Reservation;
import dotburgas.wallet.model.Wallet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Country country;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String profilePicture;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL)
    private Wallet wallet;

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL)
    private Loyalty loyalty;


    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    // @OrderBy("checkInDate ASC") --> control which way hibernate will take the entries.
    private List<Reservation> reservations = new ArrayList<>();
}
