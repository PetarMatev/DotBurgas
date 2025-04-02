package dotburgas;

import dotburgas.apartment.model.Apartment;
import dotburgas.apartment.service.ApartmentService;
import dotburgas.reporting.service.ReportingService;
import dotburgas.reservation.model.Reservation;
import dotburgas.reservation.repository.ReservationRepository;
import dotburgas.reservation.service.ReservationService;
import dotburgas.user.model.User;
import dotburgas.user.repository.UserRepository;
import dotburgas.user.service.UserService;
import dotburgas.wallet.service.WalletService;
import dotburgas.web.dto.ReservationRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dotburgas.TestBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class ReservationITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ApartmentService apartmentService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UserService userService;

    @BeforeEach
    void clean() {
        reservationRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
    }

    @Test
    @Transactional
    void createReservation_shouldCreateReservationWithCorrectDetails() {
        // Given
        User user = aRandomUser();
        user = userRepository.saveAndFlush(user);

        Apartment apartment = aRandomApartment();

        apartmentService.save(apartment);

        ReservationRequest request = aRandomReservationRequest();

        User searchedUser = userService.getById(user.getId());
        Apartment searchedApartment = apartmentService.getById(apartment.getId());

        // When
        reservationService.createReservation(
                searchedUser,  // Use the managed user instance
                searchedApartment.getId(),
                request,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail()
        );

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(1);

        Reservation createdReservation = reservations.getFirst();

        assertThat(createdReservation.getUser().getId()).isEqualTo(user.getId());
        assertThat(createdReservation.getApartment().getId()).isEqualTo(apartment.getId());
        assertThat(createdReservation.getCheckInDate()).isEqualTo(request.getCheckInDate());
        assertThat(createdReservation.getCheckOutDate()).isEqualTo(request.getCheckOutDate());
        assertThat(createdReservation.getGuests()).isEqualTo(request.getGuests());
    }
}