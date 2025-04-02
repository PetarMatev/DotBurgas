package dotburgas.user;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.loyalty.model.LoyaltyTier;
import dotburgas.loyalty.service.LoyaltyService;
import dotburgas.notification.service.NotificationService;
import dotburgas.shared.exception.DomainException;
import dotburgas.shared.exception.UsernameAlreadyExistException;
import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.user.model.Country;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.repository.UserRepository;
import dotburgas.user.service.UserService;
import dotburgas.wallet.model.Wallet;
import dotburgas.wallet.service.WalletService;
import dotburgas.web.dto.RegisterRequest;
import dotburgas.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// 1. Create the test class
// 2. Annotate the class with @ExtendWith(MockitoExtension.class)
// 3. Get the class you want to test
// 4. Get all dependencies of that class and annotate them with @Mock
// 5. Inject all those dependencies to the class we test with annotation @InjectMocks

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private WalletService walletService;
    @Mock
    private NotificationService notificationService;


    @InjectMocks
    private UserService userService;

    // 01. Register
    // Test 1: When User exists with this username -> Exception
    @Test
    void givenExistingUsername_whenRegister_thenExceptionIsThrown() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Petar123")
                .password("123123")
                .country(Country.BULGARIA)
                .build();

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(new User()));

        // When & Then
        assertThrows(UsernameAlreadyExistException.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(any());
        verify(loyaltyService, never()).createDefaultLoyaltyProgram(any());
        verify(walletService, never()).createNewWallet(any());
        verify(notificationService, never()).saveNotificationPreference(any(UUID.class), anyBoolean(), anyString());
    }

    // Test 2: Happy Path Registration
    @Test
    void givenHappyPath_whenRegister() {

        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Petar123")
                .password("123123")
                .country(Country.BULGARIA)
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);
        when(loyaltyService.createDefaultLoyaltyProgram(user)).thenReturn(new Loyalty());
        when(walletService.createNewWallet(user)).thenReturn(new Wallet());

        // When
        User registeredUser = userService.register(registerRequest);
        assertInstanceOf(Loyalty.class, registeredUser.getLoyalty());
        assertInstanceOf(Wallet.class, registeredUser.getWallet());

        // Then
        verify(notificationService, times(1)).saveNotificationPreference(user.getId(), false, null);
        verify(loyaltyService, times(1)).createDefaultLoyaltyProgram(user);
        verify(walletService, times(1)).createNewWallet(user);
    }

    // Test .02 editUserDetails
    // Test Case: when there is no user in the DB (repository returns Optional.empty())
    // then expect an exception of type DomainException is thrown.
    @Test
    void givenMissingFromDatabase_whenEditUserDetails_thenExceptionIsThrown() {

        UUID userId = UUID.randomUUID();
        UserEditRequest dto = UserEditRequest.builder().build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> userService.editUserDetails(userId, dto));
    }

    // Test Case: When Database returns user object -> then change their details
    // from the dto with email address
    // and save notification Preference and save the user to the database
    @Test
    void givenExistingUser_whenEditTheirProfileWithActualEmail_thenChangeTheirDetailsSaveNotificationPreferenceAndSaveToDatabase() {

        // Given
        UUID userId = UUID.randomUUID();
        UserEditRequest dto = UserEditRequest.builder()
                .firstName("Petar")
                .lastName("Matev")
                .email("petar_matev@yahoo.co.uk")
                .profilePicture("www.image.com")
                .build();

        User user = User.builder().build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.editUserDetails(userId, dto);

        // Then
        assertEquals("Petar", user.getFirstName());
        assertEquals("Matev", user.getLastName());
        assertEquals("petar_matev@yahoo.co.uk", user.getEmail());
        assertEquals("www.image.com", user.getProfilePicture());

        verify(notificationService, times(1)).saveNotificationPreference(userId, true, dto.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenExistingUser_whenEditTheirProfileWithEmptyEmail_thenChangeTheirDetailsSaveNotificationPreferenceAndSaveToDatabase() {

        // Given
        UUID userId = UUID.randomUUID();
        UserEditRequest dto = UserEditRequest.builder()
                .firstName("Petar")
                .lastName("Matev")
                .email("")
                .profilePicture("www.image.com")
                .build();

        User user = User.builder().build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When

        userService.editUserDetails(userId, dto);

        // Then

        assertEquals("Petar", user.getFirstName());
        assertEquals("Matev", user.getLastName());
        assertEquals("", user.getEmail());
        assertEquals("www.image.com", user.getProfilePicture());

        verify(notificationService, times(1)).saveNotificationPreference(userId, false, null);
        verify(userRepository, times(1)).save(user);
    }

    @ParameterizedTest
    @MethodSource("userRolesArguments")
    void whenChangeUserRole_thenCorrectRoleIsAssigned(UserRole currentUserRole, UserRole expectedUserRole) {

        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .role(currentUserRole)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.switchRole(userId);

        // Then (Expected // Actual)
        assertEquals(expectedUserRole, user.getRole());

    }

    private static Stream<Arguments> userRolesArguments() {
        return Stream.of(
                Arguments.of(UserRole.ADMIN, UserRole.USER),
                Arguments.of(UserRole.USER, UserRole.ADMIN)
        );
    }

    // 03. getAllUsers
    @Test
    void givenUsersExistingInDatabase_whenGetAllUsers_thenReturnThemAll() {

        // Given
        List<User> userList = List.of(new User(), new User());
        // when we call the userRepository findAll method then return userList
        when(userRepository.findAll()).thenReturn(userList);

        // When
        List<User> users = userService.getAllUsers();

        // Then
        assertThat(users).hasSize(2);
        assertEquals(2, users.size());
    }

    // 04. loadUserByUsername
    // Test 1: When user does not exist - then throw exception
    @Test
    void whenMissingUserFromDatabase_whenLoadUserByUserName_thenExceptionIsThrown() {

        // Given
        String username = "Draganchooo";

        // When
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(username));
    }

    // Test 2: When user exist - then return new AuthenticationUserDetails
    @Test
    void whenGivenExistingUserFromDatabase_whenLoadUserByUsername_thenReturnCorrectAuthenticationUserDetails() {

        // Given
        String username = "Petar123";
        User user = User.builder()
                .id(UUID.randomUUID())
                .password("123123")
                .role(UserRole.ADMIN)
                .build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        UserDetails authenticationUserDetails = userService.loadUserByUsername(username);

        // Then
        assertInstanceOf(AuthenticationUserDetails.class, authenticationUserDetails);
        AuthenticationUserDetails result = (AuthenticationUserDetails) authenticationUserDetails;
        assertEquals(user.getId(), result.getUserId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getRole(), result.getRole());
        assertThat(result.getAuthorities()).hasSize(1);
        assertEquals("ROLE_ADMIN", result.getAuthorities().iterator().next().getAuthority());
    }

    // 05. switchRole
    @Test
    void givenUserWithAdminRole_whenSwitchRole_thenUserRoleBecomeAdmin() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.USER)
                .build();
        UUID userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.switchRole(userId);

        // Then
        assertEquals(UserRole.ADMIN, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenUserWithAdminRole_whenSwitchRole_thenUserRoleBecomeUser() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.ADMIN)
                .build();
        UUID userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.switchRole(userId);

        // Then
        assertEquals(UserRole.USER, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    // 06. getUserByUsername
    @Test
    void givenUsernameThatDoesNotExistInDatabase_thenThrowException() {

        // Given
        String username = "Petar505";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> userService.getUserByUsername(username));
    }

    @Test
    void givenUsernameThatExistInDatabase_thenReturnUser() {

        // Given
        String username = "Petar505";
        User user = User.builder()
                .username(username)
                .build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        User returnedUser = userService.getUserByUsername(username);

        // Then (Expected // Actual)
        assertEquals(user.getUsername(), returnedUser.getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    // 07. initilizeUser
    @Test
    void givenRegisterRequestDto_thenReturnInitializedUser() {

        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Petar123")
                .password("123123")
                .country(Country.BULGARIA)
                .build();


        String encodedPassword = "encode_123123";
        UUID userId = UUID.randomUUID();

        Loyalty loyalty = Loyalty.builder()
                .loyaltyTier(LoyaltyTier.LEVEL_01)
                .build();

        Wallet wallet = Wallet.builder()
                .build();

        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            user.setLoyalty(loyalty);
            user.setWallet(wallet);
            return user;
        });

        when(loyaltyService.createDefaultLoyaltyProgram(any(User.class))).thenReturn(loyalty);
        when(walletService.createNewWallet(any(User.class))).thenReturn(wallet);

//        doNothing().when(notificationService).saveNotificationPreference(userId, false, "petar_matev@yahoo.co.uk");

        // When
        User registeredUser = userService.register(registerRequest);

        // Then
        assertNotNull(registeredUser);
        assertEquals("Petar123", registeredUser.getUsername());
        assertEquals(encodedPassword, registeredUser.getPassword());
        assertEquals(Country.BULGARIA, registeredUser.getCountry());
        assertEquals(UserRole.USER, registeredUser.getRole());
        assertNotNull(registeredUser.getCreatedOn());
        assertNotNull(registeredUser.getUpdatedOn());
        assertNotNull(registeredUser.getLoyalty());
        assertNotNull(registeredUser.getWallet());
    }

    // 08. getById
    @Test
    void givenUserUUIDwhichDoesNotExistInDataba_ThrowException() {

        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(DomainException.class, () -> userService.getById(userId));
    }

    @Test
    void givenHappyPath_thenReturnUser() {

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        User actualUser = userService.getById(userId);

        assertEquals(user, actualUser);
        assertEquals(userId, actualUser.getId());
        verify(userRepository, times(1)).findById(userId);
    }
}
