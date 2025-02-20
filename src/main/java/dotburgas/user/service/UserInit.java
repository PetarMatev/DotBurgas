package dotburgas.user.service;

import dotburgas.user.model.Country;
import dotburgas.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserInit implements CommandLineRunner {

    private final UserService userService;

    @Autowired
    public UserInit(UserService userService) {
        this.userService = userService;
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
    }
}
