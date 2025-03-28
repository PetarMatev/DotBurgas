package dotburgas.web;

import dotburgas.reservation.service.ReservationService;
import dotburgas.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
public class UserControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ReservationService reservationService;

    @Autowired
    private MockMvc mockMvc;

}
