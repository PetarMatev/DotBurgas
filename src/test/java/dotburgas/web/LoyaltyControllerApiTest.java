package dotburgas.web;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.loyalty.model.LoyaltyTier;
import dotburgas.loyalty.service.LoyaltyService;
import dotburgas.reporting.client.dto.ReservationResponse;
import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static dotburgas.TestBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoyaltyController.class)
public class LoyaltyControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private LoyaltyService loyaltyService;

    @Autowired
    private MockMvc mockMvc;

    // 01. getUpgradePage
    @Test
    void getUpgradePage_shouldReturnLoyaltiesView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                userId,
                "User123",
                "123123",
                UserRole.USER
        );

        // 2. Send Request
        mockMvc.perform(get("/loyalties")
                        .with(user(principal)))  // Moved inside perform()
                .andExpect(status().isOk())
                .andExpect(view().name("loyalties"))
                .andExpect(model().attributeExists("loyaltySubscriptionEditRequest"));
    }

    @Test
    void updateLoyaltySubscription_withValidRequest_shouldRedirectToHome() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        LoyaltyTier newTier = LoyaltyTier.LEVEL_01;

        User user = new User();
        Loyalty loyalty = mock(Loyalty.class);
        when(loyalty.getId()).thenReturn(loyaltyId);
        user.setLoyalty(loyalty);

        when(userService.getById(userId)).thenReturn(user);

        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                userId,
                "User123",
                "123123",
                UserRole.USER
        );

        // 2. Send Request
        mockMvc.perform(post("/loyalties")
                        .param("loyaltyTier", newTier.toString())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"));
    }

    @Test
    void updateLoyaltySubscription_validRequest_shouldWork() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        User mockedUser = mock(User.class);
        Loyalty mockedLoyalty = mock(Loyalty.class);

        when(mockedUser.getLoyalty()).thenReturn(mockedLoyalty);
        when(mockedLoyalty.getId()).thenReturn(loyaltyId);
        when(userService.getById(userId)).thenReturn(mockedUser);

        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                userId,
                "User123",
                "123123",
                UserRole.USER
        );

        // 2. Send Request
        mockMvc.perform(post("/loyalties")
                        .param("loyaltyTier", "GOLD") // Required field
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("loyalties"))
                .andExpect(model().attributeExists("loyaltySubscriptionEditRequest"));
    }

    // 03. getLoyaltyHistory
    @Test
    void getLoyaltyHistory_shouldReturnLoyaltyHistoryView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        User mockedUser = aRandomUser();
        mockedUser.setId(userId);

        when(userService.getById(userId)).thenReturn(mockedUser);

        // 2. Send Request
        AuthenticationUserDetails principal = new AuthenticationUserDetails(
                userId,
                "User123",
                "123123",
                UserRole.USER
        );

        mockMvc.perform(get("/loyalties/history")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("loyalty-history"))
                .andExpect(model().attributeExists("user"));
    }
}
