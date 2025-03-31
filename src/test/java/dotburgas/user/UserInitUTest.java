package dotburgas.user;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.user.model.Country;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.repository.UserRepository;
import dotburgas.user.service.UserInit;
import dotburgas.user.service.UserService;
import dotburgas.wallet.model.Wallet;
import dotburgas.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserInitUTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserInit userInit;

    private static final String DEFAULT_ADMIN_USERNAME = "Petar123";
    private static final User DEFAULT_ADMIN_USER = User.builder()
            .id(UUID.randomUUID())
            .username(DEFAULT_ADMIN_USERNAME)
            .password("123123")
            .country(Country.BULGARIA)
            .firstName("Petar")
            .lastName("Matev")
            .email("petar_matev@yahoo.co.uk")
            .role(UserRole.ADMIN)
            .profilePicture("www.abc.com")
            .createdOn(LocalDateTime.now())
            .updatedOn(LocalDateTime.now())
            .wallet(Wallet.builder().build())
            .loyalty(Loyalty.builder().build())
            .build();

    @BeforeEach
    void setUp() {
        DEFAULT_ADMIN_USER.setRole(UserRole.ADMIN);
    }

    @Test
    void run_ShouldDoNothing_WhenUsersExist() throws Exception {

        // Given
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .username(DEFAULT_ADMIN_USERNAME)
                .password("123123")
                .country(Country.BULGARIA)
                .firstName("Petar")
                .lastName("Matev")
                .email("petar_matev@yahoo.co.uk")
                .role(UserRole.ADMIN)
                .profilePicture("www.abc.com")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .wallet(Wallet.builder().build())
                .loyalty(Loyalty.builder().build())
                .build();

        when(userService.getAllUsers()).thenReturn(List.of(existingUser));

        // When
        userInit.run();

        // Then
        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void run_ShouldCreateNewUser_WhenNoUsersExist() throws Exception {

        // Given
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .username(DEFAULT_ADMIN_USERNAME)
                .password("123123")
                .country(Country.BULGARIA)
                .firstName("Petar")
                .lastName("Matev")
                .email("petar_matev@yahoo.co.uk")
                .role(UserRole.ADMIN)
                .profilePicture("www.abc.com")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .wallet(Wallet.builder().build())
                .loyalty(Loyalty.builder().build())
                .build();

        when(userService.getAllUsers()).thenReturn(List.of());
        when(userService.getUserByUsername(DEFAULT_ADMIN_USERNAME)).thenReturn(existingUser);

        // When
        userInit.run();

        // Then
        verify(userService).register(any(RegisterRequest.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void setDefaultAdmin_ShouldUpdateUserDetails_WhenUserExists() {
        // Given
        when(userService.getUserByUsername(DEFAULT_ADMIN_USERNAME)).thenReturn(DEFAULT_ADMIN_USER);

        // When
        userInit.setDefaultAdmin();

        // Then
        assertEquals(UserRole.ADMIN, DEFAULT_ADMIN_USER.getRole());
        assertEquals("Petar", DEFAULT_ADMIN_USER.getFirstName());
        assertEquals("Matev", DEFAULT_ADMIN_USER.getLastName());
        assertEquals("petargmatev@gmail.com", DEFAULT_ADMIN_USER.getEmail());
        assertEquals("/img/admin-photo.jpg", DEFAULT_ADMIN_USER.getProfilePicture());
        verify(userRepository).save(DEFAULT_ADMIN_USER);
    }

    @Test
    void setDefaultAdmin_ShouldThrowException_WhenUserDoesNotExist() {

        // Given
        when(userService.getUserByUsername(DEFAULT_ADMIN_USERNAME)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userInit.setDefaultAdmin());
        assertEquals("Default admin user could not be found after registration.", exception.getMessage());
    }
}