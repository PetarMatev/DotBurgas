package dotburgas.web;

import dotburgas.apartment.service.ApartmentService;
import dotburgas.shared.exception.UsernameAlreadyExistException;
import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.user.model.UserRole;
import dotburgas.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static dotburgas.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ApartmentService apartmentService;

    // used to send requests
    @Autowired
    private MockMvc mockMvc;

    // Send GET /
    // Result - view name index
    @Test
    void getRequestToIndexEndpoint_shouldReturnIndexView() throws Exception {

        // 01. Build Request
        MockHttpServletRequestBuilder request = get("/");

        // 02. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getRequestToRegisterEndpoint_shouldReturnRegisterView() throws Exception {

        // 01. Build Request
        MockHttpServletRequestBuilder request = get("/register");

        // .andExpect() -> our assertMethods
        // MockMvcResultMatchers.status() - checking the status
        // model().attributeExists("registerRequest") - checks if there is concrete attribute that is "NOT NULL"

        // 02. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void getRequestToPrivacyEndpoint_shouldReturnPrivacyView() throws Exception {

        // 01. Build Request
        MockHttpServletRequestBuilder request = get("/privacy");

        // 02. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("privacy"));
    }

    @Test
    void getRequestToContactEndpoint_shouldReturnContactView() throws Exception {

        // 01. Build Request
        MockHttpServletRequestBuilder request = get("/contact");

        // 02. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("contact"));
    }

    @Test
    void getRequestToDiscoverBurgasEndpoint_shouldReturnDiscoverBurgasView() throws Exception {

        // 01. Build Request
        MockHttpServletRequestBuilder request = get("/discover-burgas");

        // 02. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("discover-burgas"));
    }

    @Test
    void getRequestToAboutEndpoint_shouldReturnAboutView() throws Exception {

        // 01. Build Request
        MockHttpServletRequestBuilder request = get("/about");

        // 02. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("about"));
    }

    @Test
    void getRequestToAccommodationPageEndpoint_shouldReturnAccommodationPageView() throws Exception {

        // 01. Build Request
        MockHttpServletRequestBuilder request = get("/accommodation");

        // 02. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("accommodation"))
                .andExpect(model().attributeExists("apartments"));
    }

    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginView() throws Exception {

        // 01. Build Request
        MockHttpServletRequestBuilder request = get("/login");

        // 02. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void getRequestToLoginEndpointWithErrorParameter_shouldReturnLoginViewAndErrorMessageAttribute() throws Exception {

        // 01. Build Request
        MockHttpServletRequestBuilder request = get("/login").param("error", "");

        // 02. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"))
                .andExpect(model().attributeExists("errorMessage"));
    }


    // POST with correct data form
    // Expect:
    // status - 3xx ok Redirect
    // called .register method for userService
    // redirect to /login
    @Test
    void postRequestToRegisterEndpoint_happyPath() throws Exception {

        // 01. Build Request
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Petar123")
                .formField("password", "123123")
                .formField("country", "BULGARIA")
                .with(csrf()); // a Site Request Forgery (CSRF) error

        // 02. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(userService, times(1)).register(any());
    }

    @Test
    void postRequestToRegisterEndpoint_WhenUsernameAlreadyExist_thenRedirectToRegisterWithFlashParameter() throws Exception {

        // 01. Build Request

        when(userService.register(any())).thenThrow(new UsernameAlreadyExistException("Username already exist!"));
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Petar123")
                .formField("password", "123123")
                .formField("country", "BULGARIA")
                .with(csrf()); // a Site Request Forgery (CSRF) error

        // 02. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"));
//                .andExpect(flash().attributeExists("usernameAlreadyExistMessage"));
        verify(userService, times(1)).register(any());
    }

    @Test
    void postRequestToRegisterEndpointWithInvalidData_returnRegisterView() throws Exception {

        // 01. Build Request
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "")
                .formField("password", "")
                .formField("country", "BULGARIA")
                .with(csrf()); // a Site Request Forgery (CSRF) error

        // 02. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
        verify(userService, never()).register(any());
    }

    @Test
    void getAuthenticatedRequestToHome_returnHomeView() throws Exception {

        // 01. Build Request
        when(userService.getById(any())).thenReturn(aRandomUser());

        UUID userId = UUID.randomUUID();
        AuthenticationUserDetails principal = new AuthenticationUserDetails(userId, "User123", "123123", UserRole.USER);
        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal));

        // 02. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"));
        verify(userService, times(1)).getById(userId);
    }


    @Test
    void getUnAuthenticatedRequestToHome_redirectToLogin() throws Exception {

        // 01. Build Request
        MockHttpServletRequestBuilder request = get("/home")
                .with(csrf());

        // 02. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
        verify(userService, never()).getById(any());
    }
}
