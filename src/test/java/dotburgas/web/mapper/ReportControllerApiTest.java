package dotburgas.web.mapper;

import dotburgas.reporting.client.dto.ReservationResponse;
import dotburgas.reporting.client.dto.ReservationStatsResponse;
import dotburgas.reporting.service.ReportingService;
import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.service.UserService;
import dotburgas.web.ReportController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static dotburgas.TestBuilder.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ReportController.class)
public class ReportControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ReportingService reportingService;

    @Autowired
    private MockMvc mockMvc;

    // 01. getReservationPage
    @Test
    void getReservationPage_shouldReturnReportingSvcEndPoint() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        User mockedUser = aRandomUser();
        mockedUser.setId(userId);
        ReservationResponse reservationResponseOne = aRandomReservationResponse();
        ReservationResponse reservationResponseTwo = aRandomReservationResponse();
        List<ReservationResponse> listOfReservationResponse = List.of(reservationResponseOne, reservationResponseTwo);
        when(userService.getById(mockedUser.getId())).thenReturn(mockedUser);
        when(reportingService.getReservationHistory()).thenReturn(listOfReservationResponse);

        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                UUID.randomUUID(),
                "User123",
                "123123",
                UserRole.USER
        );

        // 2. Send Request
        mockMvc.perform(get("/admin/reporting")
                        .with(user(principal))) // Add proper authentication
                .andExpect(status().isOk())
                .andExpect(view().name("reporting-svc"))
                .andExpect(model().attributeExists("reservationHistory"));
    }

    // 02. getStatsPage
    @Test
    void getStats_shouldReturnAnalyticsView() throws Exception {

        // 1. Build Request
        ReservationStatsResponse statsOne = aRandomReservationStatsResponse();
        ReservationStatsResponse statsTwo = aRandomReservationStatsResponse();
        List<ReservationStatsResponse> listOfReservationStatsResponse = List.of(statsOne, statsTwo);

        when(reportingService.getSummaryStatsPerApartment()).thenReturn(listOfReservationStatsResponse);

        // 2. Send Request

        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                UUID.randomUUID(),
                "User123",
                "123123",
                UserRole.USER
        );

        mockMvc.perform(get("/admin/reporting/stats")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("analytics"))
                .andExpect(model().attributeExists("reservationStatsResponse"));
    }

    // 03.retrieveReservationDetails
    @Test
    void getRetrieveReservationDetails_thenShouldReturnQueryView() throws Exception {

        // 1. Build Request
        UUID reservationID = UUID.randomUUID();
        ReservationResponse reservationResponse = aRandomReservationResponse();
        aRandomReservationResponse().setReservationId(reservationID);

        when(reportingService.getReservationDetails(reservationID)).thenReturn(reservationResponse);

        // 2. Send Request
        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                UUID.randomUUID(),
                "User123",
                "123123",
                UserRole.USER
        );

        mockMvc.perform(get("/admin/reporting/query")
                        .param("reservationId", reservationID.toString())
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("query"))
                .andExpect(model().attributeExists("reservationResponse"));

    }
}
