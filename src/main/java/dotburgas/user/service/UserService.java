package dotburgas.user.service;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.loyalty.service.LoyaltyService;
import dotburgas.shared.exception.DomainException;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.repository.UserRepository;
import dotburgas.wallet.model.Wallet;
import dotburgas.wallet.service.WalletService;
import dotburgas.web.dto.LoginRequest;
import dotburgas.web.dto.RegisterRequest;
import dotburgas.web.dto.UserEditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoyaltyService loyaltyService;
    private final WalletService walletService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, LoyaltyService loyaltyService, WalletService walletService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loyaltyService = loyaltyService;
        this.walletService = walletService;
    }

    public User login(LoginRequest loginRequest) {

        Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());
        // if we don't have user with this username return exception.
        if (optionalUser.isEmpty()) {
            throw new DomainException("Username or password are incorrect");
        }

        User user = optionalUser.get();
        // we get the clean password from the loginRequest and compare it to the encrypted version using matches.
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new DomainException("Username or password are incorrect");
        }

        // if everything is okay we return the user
        return user;
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionalUser = userRepository.findByUsername(registerRequest.getUsername());
        if (optionalUser.isPresent()) {
            throw new DomainException("Username [%s] already exists.".formatted(registerRequest.getUsername()));
        }

        User user = userRepository.save(initilizeUser(registerRequest));

        Loyalty defaultLoyalty = loyaltyService.createDefaultLoyaltyProgram(user);
        user.setLoyalty(defaultLoyalty);

        Wallet defaultWallet = walletService.createNewWallet(user);
        user.setWallet(defaultWallet);

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

    // initial call on the method saves the result in a cache but every other time it is called still involk the original data saved in the cahe
    @Cacheable("users")
    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    public User getById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new DomainException("User with id [%s] does not exist.".formatted(id)));
    }


    @CacheEvict(value = "users", allEntries = true)
    public void switchUserRole(UUID userId) {

        User user = userRepository.getById(userId);

        if (user.getRole() == UserRole.ADMIN) {
            user.setRole(UserRole.USER);
        } else {
            user.setRole(UserRole.ADMIN);
        }
        userRepository.save(user);
    }
}
