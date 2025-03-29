package dotburgas.web;

import dotburgas.reservation.service.ReservationService;
import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.service.UserService;
import dotburgas.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {

   @MockitoBean
   private UserService userService;

   @MockitoBean
   private ReservationService reservationService;

   @Autowired
   private MockMvc mockMvc;

   // 01. getProfileMenu
   @Test
   void getProfileMenu_shouldReturnUserProfile() throws Exception {

      // 01. Build Request
      UUID userId = UUID.randomUUID();
      User mockUser = User.builder()
              .firstName("Test")
              .lastName("User")
              .email("testuser@email.com")
              .build();
      when(userService.getById(userId)).thenReturn(mockUser);

      // 2. Perform GET Request
      AuthenticationUserDetails principal = new AuthenticationUserDetails(userId, "User123", "123123", UserRole.USER);

      mockMvc.perform(get("/users/{id}/profile", userId)
                      .with(user(principal)))
              .andExpect(status().isOk())
              .andExpect(view().name("profile"))
              .andExpect(model().attributeExists("user"))
              .andExpect(model().attribute("user", mockUser))
              .andExpect(model().attributeExists("userEditRequest"));
   }

   // 02. updateUserProfile
   @Test
   void putRequestUpdateUserProfile_shouldRedirectToHome() throws Exception {

      // 1. Build Request
      UUID userId = UUID.randomUUID();
      UserEditRequest userEditRequest = UserEditRequest.builder()
              .firstName("UpdatedFirstName")
              .lastName("UpdatedLastName")
              .email("updated@email.com")
              .profilePicture("www.test.com")
              .build();
      AuthenticationUserDetails principal = new AuthenticationUserDetails(userId, "User123", "123123", UserRole.USER);

      // 2. Send Request
      MockHttpServletRequestBuilder request = put("/users/{id}/profile", userId)
              .with(user(principal))
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .param("firstName", userEditRequest.getFirstName())
              .param("lastName", userEditRequest.getLastName())
              .param("email", userEditRequest.getEmail())
              .with(csrf());

      mockMvc.perform(request)
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/home"))
              .andExpect(model().attributeDoesNotExist("userEditRequest"));
      verify(userService, times(1)).editUserDetails(eq(userId), any(UserEditRequest.class));
   }


   // 03. updateUserProfile - Validation Error Case
   @Test
   public void updateUserProfile_WithInvalidData_ShouldReturnProfileViewWithErrors() throws Exception {

      // 1. Build Request
      UUID userId = UUID.randomUUID();
      User mockUser = User.builder()
              .id(userId)
              .firstName("Existing")
              .lastName("User")
              .email("existing@email.com")
              .build();

      when(userService.getById(userId)).thenReturn(mockUser);

      AuthenticationUserDetails principal = new AuthenticationUserDetails(
              userId, "testuser", "password", UserRole.USER);

      // 2. Send Request
      mockMvc.perform(put("/users/{id}/profile", userId)
                      .with(user(principal))
                      .with(csrf())
                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .param("firstName", "")
                      .param("lastName", "")
                      .param("email", "invalid-email")
                      .param("profilePicture", "not-a-url"))
              .andExpect(status().isOk())
              .andExpect(view().name("profile"))
              .andExpect(model().attributeExists("user"))
              .andExpect(model().attributeExists("userEditRequest"))
              .andExpect(model().errorCount(2));
   }
}
