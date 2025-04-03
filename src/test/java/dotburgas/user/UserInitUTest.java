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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserInitUTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserInit userInit;

    private static final String DEFAULT_ADMIN_USERNAME = "Petar123";
    private static final String DEFAULT_ADMIN_PASSWORD = "123123";

    private User defaultAdminUser;

    @BeforeEach
    void setUp() {
        defaultAdminUser = User.builder()
                .id(UUID.randomUUID())
                .username(DEFAULT_ADMIN_USERNAME)
                .password(DEFAULT_ADMIN_PASSWORD)
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
    }

    @Test
    void run_ShouldDoNothing_WhenUsersExist() throws Exception {

        // Given
        when(userRepository.findByUsername(DEFAULT_ADMIN_USERNAME))
                .thenReturn(java.util.Optional.of(defaultAdminUser));

        // When
        userInit.run();

        // Then
        verify(userService, never()).register(any());
    }

    @Test
    void run_ShouldCreateNewUser_WhenNoUsersExist() throws Exception {

        // Given
        when(userRepository.findByUsername(DEFAULT_ADMIN_USERNAME))
                .thenReturn(Optional.empty());


        User mockUser = User.builder()
                .username(DEFAULT_ADMIN_USERNAME)
                .build();

        when(userService.register(any(RegisterRequest.class)))
                .thenReturn(mockUser);

        when(userService.getUserByUsername(DEFAULT_ADMIN_USERNAME))
                .thenReturn(mockUser);

        // When
        userInit.run();

        // Then
        verify(userService).register(argThat(request ->
                request.getUsername().equals(DEFAULT_ADMIN_USERNAME) &&
                        request.getPassword().equals(DEFAULT_ADMIN_PASSWORD) &&
                        request.getCountry() == Country.BULGARIA
        ));
        verify(userRepository).save(mockUser);
    }

    @Test
    void setDefaultAdmin_ShouldUpdateUserDetails_WhenUserExists() {

        // Given
        when(userService.getUserByUsername(DEFAULT_ADMIN_USERNAME))
                .thenReturn(defaultAdminUser);

        // When
        userInit.setDefaultAdmin();

        // Then
        verify(userRepository).save(defaultAdminUser);
        assertEquals(UserRole.ADMIN, defaultAdminUser.getRole());
        assertEquals("Petar", defaultAdminUser.getFirstName());
        assertEquals("Matev", defaultAdminUser.getLastName());
        assertEquals("petargmatev@gmail.com", defaultAdminUser.getEmail());
        assertEquals("/img/admin-photo.jpg", defaultAdminUser.getProfilePicture());
    }

    @Test
    void setDefaultAdmin_ShouldThrowException_WhenUserDoesNotExist() {

        // Given
        when(userService.getUserByUsername(DEFAULT_ADMIN_USERNAME))
                .thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userInit.setDefaultAdmin()
        );
        assertEquals("Default admin user could not be found after registration.", exception.getMessage());
        verify(userService).getUserByUsername(DEFAULT_ADMIN_USERNAME);
    }
}