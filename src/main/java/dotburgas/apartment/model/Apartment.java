package dotburgas.apartment.model;

import dotburgas.reservation.model.Reservation;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID Id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApartmentType type;

    @OneToMany(mappedBy = "apartment", fetch = FetchType.EAGER)
    private List<Reservation> reservations;
}
