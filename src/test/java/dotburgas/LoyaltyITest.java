package dotburgas;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.loyalty.model.LoyaltyTier;
import dotburgas.loyalty.repository.LoyaltyRepository;
import dotburgas.loyalty.service.LoyaltyService;
import dotburgas.notification.client.dto.Notification;
import dotburgas.notification.service.NotificationService;
import dotburgas.user.model.Country;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.repository.UserRepository;
import dotburgas.user.service.UserService;
import dotburgas.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class LoyaltyITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private LoyaltyService loyaltyService;

    @Autowired
    private LoyaltyRepository loyaltyRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private NotificationService notificationService;

    @BeforeEach
    void clean() {
        loyaltyRepository.deleteAll();
        userRepository.deleteAll();

        Notification mockNotification1 = Notification.builder()
                .subject("hello")
                .build();

        Notification mockNotification2 = Notification.builder()
                .subject("baa")
                .build();
        when(notificationService.getNotificationHistory(any()))
                .thenReturn(List.of(mockNotification1, mockNotification2));
    }


    @Test
    void whenUpdateLoyaltySubscription_thenLoyaltyTierIsUpdated() {

        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Petar123")
                .password("123123")
                .country(Country.BULGARIA)
                .build();
        User registeredUser = userService.register(registerRequest);

        Loyalty initialLoyalty = registeredUser.getLoyalty();
        assertNotNull(initialLoyalty, "Loyalty should be created automatically");
        assertEquals(LoyaltyTier.LEVEL_01, initialLoyalty.getLoyaltyTier(),
                "Initial loyalty tier should be LEVEL_01");

        // When
        loyaltyService.updatedLoyaltySubscription(initialLoyalty.getId(), LoyaltyTier.LEVEL_02);

        // Then
        Loyalty updatedLoyalty = loyaltyRepository.findById(initialLoyalty.getId())
                .orElseThrow(() -> new AssertionError("Loyalty should exist"));
        assertEquals(LoyaltyTier.LEVEL_02, updatedLoyalty.getLoyaltyTier(),
                "Loyalty tier should be updated to LEVEL_02");
        assertNotNull(updatedLoyalty.getUpdatedOn(),
                "Updated timestamp should be set");
    }

    @Test
    void createDefaultLoyaltyProgram_shouldCreateLoyaltyWithDefaultValues() {

        // Given
        User testUser = createTestUser("testUser1");

        // When
        Loyalty createdLoyalty = loyaltyService.createDefaultLoyaltyProgram(testUser);

        // Then
        assertNotNull(createdLoyalty.getId(), "Loyalty should have an ID");
        assertEquals(LoyaltyTier.LEVEL_01, createdLoyalty.getLoyaltyTier(),
                "Should create with default loyalty tier");
        assertEquals(testUser.getId(), createdLoyalty.getOwner().getId(),
                "Should be associated with the correct user");
        assertNotNull(createdLoyalty.getCreatedOn(), "Created timestamp should be set");
        assertNotNull(createdLoyalty.getUpdatedOn(), "Updated timestamp should be set");

        User userFromDb = userRepository.findById(testUser.getId()).orElseThrow();
        assertNotNull(userFromDb.getLoyalty(), "User should reference the loyalty");
        assertEquals(createdLoyalty.getId(), userFromDb.getLoyalty().getId(),
                "User's loyalty reference should match created loyalty");
    }

    @Test
    void createDefaultLoyaltyProgram_shouldFailWhenUserAlreadyHasLoyalty() {

        // Given
        User testUser = createTestUser("testUser2");
        loyaltyService.createDefaultLoyaltyProgram(testUser);

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            loyaltyService.createDefaultLoyaltyProgram(testUser);
        }, "Should throw exception when user already has loyalty");
    }

    @Test
    void createDefaultLoyaltyProgram_shouldPersistCorrectlyInDatabase() {

        // Given
        User testUser = createTestUser("testUser3");

        // When
        Loyalty createdLoyalty = loyaltyService.createDefaultLoyaltyProgram(testUser);

        // Then
        Optional<Loyalty> loyaltyFromDb = loyaltyRepository.findById(createdLoyalty.getId());
        assertTrue(loyaltyFromDb.isPresent(), "Loyalty should exist in database");
        assertEquals(testUser.getId(), loyaltyFromDb.get().getOwner().getId(),
                "Should maintain correct user association in DB");
    }

    private User createTestUser(String username) {
        User user = User.builder()
                .username(username)
                .password("testPass")
                .country(Country.BULGARIA)
                .role(UserRole.USER)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }
}
