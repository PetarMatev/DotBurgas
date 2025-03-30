package dotburgas.web;

import dotburgas.apartment.service.ApartmentService;
import dotburgas.loyalty.service.LoyaltyService;
import dotburgas.notification.service.NotificationService;
import dotburgas.reporting.service.ReportingService;
import dotburgas.reservation.service.ReservationService;
import dotburgas.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
public class NotificationControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private  ApartmentService apartmentService;

    @MockitoBean
    private  ReservationService reservationService;

    @MockitoBean
    private  ReportingService reportingService;

    @Autowired
    private MockMvc mockMvc;
}
