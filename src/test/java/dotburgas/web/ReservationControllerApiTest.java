package dotburgas.web;

import dotburgas.loyalty.service.LoyaltyService;
import dotburgas.notification.service.NotificationService;
import dotburgas.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReservationController.class)
public class ReservationControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private  NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;
}
