package dotburgas.user.service;

import dotburgas.user.model.Country;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.repository.UserRepository;
import dotburgas.web.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile("!test")
@ConditionalOnProperty(value = "user.init.enabled", havingValue = "true")
public class UserInit implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;
    private static final String DEFAULT_ADMIN_USERNAME = "Petar123";
    private static final String DEFAULT_ADMIN_PASSWORD = "123123";
    private static final String DEFAULT_ADMIN_FIRST_NAME = "Petar";
    private static final String DEFAULT_ADMIN_LAST_NAME = "Matev";
    private static final String DEFAULT_ADMIN_EMAIL = "petargmatev@gmail.com";
    private static final String DEFAULT_ADMIN_PHOTO = "/img/admin-photo.jpg";


    @Autowired
    public UserInit(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }


    @Override
    public void run(String... args) throws Exception {
        User existingAdmin = userRepository.findByUsername(DEFAULT_ADMIN_USERNAME).orElse(null);

        if (existingAdmin != null) {
            // if the list is not empty, then just return, don't do anything.
            return;
        }

        log.info("Creating default admin user");
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(DEFAULT_ADMIN_USERNAME)
                .password(DEFAULT_ADMIN_PASSWORD)
                .country(Country.BULGARIA)
                .build();

        userService.register(registerRequest);

        setDefaultAdmin();
    }

    public void setDefaultAdmin() {
        User user = userService.getUserByUsername(DEFAULT_ADMIN_USERNAME);
        if (user != null) {
            user.setRole(UserRole.ADMIN);
            user.setFirstName(DEFAULT_ADMIN_FIRST_NAME);
            user.setLastName(DEFAULT_ADMIN_LAST_NAME);
            user.setEmail(DEFAULT_ADMIN_EMAIL);
            user.setProfilePicture(DEFAULT_ADMIN_PHOTO);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Default admin user could not be found after registration.");
        }
    }
}
