package dotburgas;

import dotburgas.loyalty.repository.LoyaltyRepository;
import dotburgas.loyalty.service.LoyaltyService;
import dotburgas.user.repository.UserRepository;
import dotburgas.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest // Integration Test (Load the complete Spring Application Context - all beans)
public class LoyaltyITest {

    @Autowired
    private UserService userService;

    @Autowired
    private LoyaltyService loyaltyService;

    @Autowired
    private LoyaltyRepository loyaltyRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void clean() {

        userRepository.deleteAll();
    }

//    @Test
//    void loyaltyToPlan_happyPath() {
//
//        // Given
//        RegisterRequest registerRequest = RegisterRequest
//                .builder()
//                .username("Petar123")
//                .password("123123")
//                .country(Country.BULGARIA)
//                .build();
//        User registeredUser = userService.register(registerRequest);
//
//
//        Loyalty loyalty = new Loyalty();
//        loyalty.setOwner(registeredUser);
//        loyalty.setLoyaltyTier(LoyaltyTier.LEVEL_01);  // Initial tier
//        loyalty.setUpdatedOn(LocalDateTime.now());
//        loyaltyRepository.save(loyalty);
//
//
//        // When
//        loyaltyService.updatedLoyaltySubscription(registeredUser.getId(), LoyaltyTier.LEVEL_02);
//
//        // Then
//        // User has one loyalty which is Level 2
//        Optional<Loyalty> optionalLoyaltyTierLevel_02 = loyaltyRepository.findByLoyaltyTierAndOwner(LoyaltyTier.LEVEL_02, registeredUser);
//        assertTrue(optionalLoyaltyTierLevel_02.isPresent());
//        assertEquals(LoyaltyTier.LEVEL_02, optionalLoyaltyTierLevel_02.get().getLoyaltyTier());
//    }
}
