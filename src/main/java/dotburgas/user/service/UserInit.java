package dotburgas.user.service;

import dotburgas.user.model.Country;
import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.repository.UserRepository;
import dotburgas.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserInit implements CommandLineRunner {

    private final UserService userService;
    private final String DEFAULT_ADMIN_USERNAME = "Petar123";
    private final UserRepository userRepository;

    @Autowired
    public UserInit(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }


    @Override
    public void run(String... args) throws Exception {

        if (!userService.getAllUsers().isEmpty()) {
            // if the list is not empty, then just return, don't do anything.
            return;
        }

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Petar123")
                .password("123123")
                .country(Country.BULGARIA)
                .build();

        userService.register(registerRequest);

        setDefaultAdmin();
    }

    public void setDefaultAdmin() {
        User user = userService.getUserByUsername(DEFAULT_ADMIN_USERNAME);
        if (user != null) {
            user.setRole(UserRole.ADMIN);
            user.setFirstName("Petar");
            user.setLastName("Matev");
            user.setEmail("petargmatev@gmail.com");
            user.setProfilePicture("static/img/admin-photo.jpg");
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Default admin user could not be found after registration.");
        }
    }
}
