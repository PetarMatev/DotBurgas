package dotburgas.user.service;

import dotburgas.shared.exception.NotificationServiceFeignCallException;
import dotburgas.shared.exception.UsernameAlreadyExistException;
import dotburgas.loyalty.model.Loyalty;
import dotburgas.loyalty.service.LoyaltyService;
import dotburgas.notification.service.NotificationService;
import dotburgas.shared.exception.DomainException;
import dotburgas.shared.security.AuthenticationUserDetails;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.repository.UserRepository;
import dotburgas.wallet.model.Wallet;
import dotburgas.wallet.service.WalletService;
import dotburgas.web.dto.RegisterRequest;
import dotburgas.web.dto.UserEditRequest;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoyaltyService loyaltyService;
    private final WalletService walletService;
    private final NotificationService notificationService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, LoyaltyService loyaltyService, WalletService walletService, JavaMailSender javaMailSender, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loyaltyService = loyaltyService;
        this.walletService = walletService;
        this.notificationService = notificationService;
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public User register(RegisterRequest registerRequest) {

        // Test whenOptionalOfUserIsPresent_thenThrowNewUsernameAlreadyExistException
        Optional<User> optionalUser = userRepository.findByUsername(registerRequest.getUsername());

        if (optionalUser.isPresent()) {
            throw new UsernameAlreadyExistException("Username [%s] already exists.".formatted(registerRequest.getUsername()));
        }

        User user = userRepository.save(initilizeUser(registerRequest));

        Loyalty defaultLoyalty = loyaltyService.createDefaultLoyaltyProgram(user);
        user.setLoyalty(defaultLoyalty);

        Wallet defaultWallet = walletService.createNewWallet(user);
        user.setWallet(defaultWallet);

        try {
            // Wrap in try-catch to make notification preference optional
            notificationService.saveNotificationPreference(user.getId(), false, null);
        } catch (Exception e) {
            log.warn("Could not set notification preferences for user {}, continuing without them", user.getId(), e);
        }

        log.info("Successfully create new user account for username [%s] and id [%s]".formatted(user.getUsername(), user.getId()));

        return user;
    }

    @CacheEvict(value = "users", allEntries = true)
    public void editUserDetails(UUID userId, UserEditRequest userEditRequest) {

        User user = getById(userId);

        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
        user.setEmail(userEditRequest.getEmail());
        user.setProfilePicture(userEditRequest.getProfilePicture());

        userRepository.save(user);

        try {
            if (userEditRequest.getEmail().isBlank()) {
                notificationService.saveNotificationPreference(userId, false, null);
            } else {
                notificationService.saveNotificationPreference(userId, true, userEditRequest.getEmail());
            }
        } catch (FeignException e) {
            log.warn("Failed to update notification preferences for user {}: {}", userId, e.getMessage());
            throw new NotificationServiceFeignCallException(
                    "Your profile was updated successfully, but we couldn't update notification preferences. Please try updating them again later."
            );
        }
    }

    // initial call on the method saves the result in a cache but every other time it is called still involk the original data saved in the cahe
    @Cacheable("users")
    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    // Everytime user logs in, spring security will call this method to get the details of the user with this username.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("user with this username does not exist"));

        return new AuthenticationUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole());
    }

    @CacheEvict(value = "users", allEntries = true)
    public void switchRole(UUID userId) {

        User user = getById(userId);

        if (user.getRole() == UserRole.USER) {
            user.setRole(UserRole.ADMIN);
        } else {
            user.setRole(UserRole.USER);
        }

        userRepository.save(user);
        log.info("The User Role for Username: %s with Id: %s has been amended to %s".formatted(user.getUsername(), user.getId(), user.getRole().name()));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User with username [%s] does not exist.".formatted(username)));
    }

    private User initilizeUser(RegisterRequest registerRequest) {
        return User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .country(registerRequest.getCountry())
                .role(UserRole.USER)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public User getById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new DomainException("User with id [%s] does not exist.".formatted(id)));
    }
}
