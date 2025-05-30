package dotburgas.web.dto;

import dotburgas.user.model.Country;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @Size(min = 6, message = "Username must be at least 6 symbols")
    private String username;

    @Size(min = 6, message = "Password must be at least 6 symbols")
    private String password;

    @NotNull
    private Country country;
}
