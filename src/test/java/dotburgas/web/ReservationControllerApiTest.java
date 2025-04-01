package dotburgas.web;

import dotburgas.apartment.service.ApartmentService;
import dotburgas.reporting.service.ReportingService;
import dotburgas.reservation.model.Reservation;
import dotburgas.reservation.service.ReservationService;
import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.service.UserService;
import dotburgas.web.dto.ReservationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static dotburgas.TestBuilder.aRandomReservation;
import static dotburgas.TestBuilder.aRandomUser;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
public class ReservationControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ApartmentService apartmentService;

    @MockitoBean
    private ReportingService reportingService;

    @MockitoBean
    private ReservationService reservationService;


    @Autowired
    private MockMvc mockMvc;

    // 1. getReservationForm
    @Test
    void getReservationForm_shouldReturnReservationRequestViewWithModelAttributes() throws Exception {

        // 1. Build Request
        UUID apartmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                userId,
                "User123",
                "123123",
                UserRole.USER
        );

        User mockUser = aRandomUser();
        mockUser.setId(userId);
        mockUser.setFirstName("Petar");
        mockUser.setLastName("Matev");
        mockUser.setEmail("petar.matev@gmail.com");
        String apartmentName = "Apartment 1";

        when(apartmentService.findApartmentNameByID(apartmentId)).thenReturn(apartmentName);
        when(userService.getById(userId)).thenReturn(mockUser);

        // 2. Send Request
        mockMvc.perform(get("/reservation-request")
                        .param("apartmentId", apartmentId.toString())
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation-request"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("apartmentName"))
                .andExpect(model().attributeExists("apartmentId"))
                .andExpect(model().attributeExists("reservationRequest"))
                .andExpect(model().attribute("apartmentName", apartmentName))
                .andExpect(model().attribute("apartmentId", apartmentId))
                .andExpect(model().attribute("reservationRequest", hasProperty("firstName", is("Petar"))))
                .andExpect(model().attribute("reservationRequest", hasProperty("lastName", is("Matev"))))
                .andExpect(model().attribute("reservationRequest", hasProperty("email", is("petar.matev@gmail.com"))));
    }

    // 2. submitReservationRequest
    @Test
    void submitReservationRequest_shouldCreateReservationAndRedirect() throws Exception {

        // 1. Build Request
        UUID apartmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                userId,
                "user123",
                "password123",
                UserRole.USER
        );

        User mockUser = aRandomUser();
        mockUser.setId(userId);

        String firstName = "Petar";
        String lastName = "Matev";
        String email = "petar.matev@abv.bg";

        ReservationRequest validRequest = new ReservationRequest();
        validRequest.setCheckInDate(LocalDate.now().plusDays(1));
        validRequest.setCheckOutDate(LocalDate.now().plusDays(3));
        validRequest.setGuests(2);

        when(userService.getById(userId)).thenReturn(mockUser);
        doNothing().when(reservationService)
                .createReservation(mockUser, apartmentId, validRequest, firstName, lastName, email);

        // 2. Send Request
        mockMvc.perform(post("/reservation-request")
                        .param("apartmentId", apartmentId.toString())
                        .param("firstName", firstName)
                        .param("lastName", lastName)
                        .param("email", email)
                        .flashAttr("reservationRequest", validRequest)
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-reservations"));
    }


    @Test
    void submitReservationRequest_shouldReturnErrorsWhenInvalid() throws Exception {

        // 1. Build Request
        UUID apartmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                userId,
                "user123",
                "password123",
                UserRole.USER
        );

        // 2. Send Request
        mockMvc.perform(post("/reservation-request")
                        .param("apartmentId", apartmentId.toString())
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("email", "invalid-email")
                        .param("checkInDate", LocalDate.now().toString())
                        .param("checkOutDate", LocalDate.now().plusDays(1).toString())
                        .param("guests", "1")
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation-request"))
                .andExpect(model().attributeHasFieldErrors(
                        "reservationRequest",
                        "firstName", "lastName", "email"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors", hasSize(greaterThan(0))));
    }

    // 3. getUserReservations
    @Test
    void getUserReservations_shouldReturnUserReservationView() throws Exception {

        // 1. Build Request
        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                UUID.randomUUID(),
                "User123",
                "123123",
                UserRole.USER
        );

        UUID userId = principal.getUserId();
        User user = aRandomUser();
        user.setId(userId);
        when(userService.getById(userId)).thenReturn(user);

        Reservation reservationOne = aRandomReservation();
        Reservation reservationTwo = aRandomReservation();
        List<Reservation> listOfUserReservation = List.of(reservationOne, reservationTwo);

        when(reservationService.getReservationsByUser(user)).thenReturn(listOfUserReservation);

        // 2. Send Request
        mockMvc.perform(get("/user-reservations")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("user-reservations"))
                .andExpect(model().attributeExists("reservations"));
    }
}
