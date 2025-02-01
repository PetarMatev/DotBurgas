package dotburgas.web.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {

    @Size(min = 6, message = "Username must be at least 6 symbols")
    public String username;

    @Size(min = 6, message = "Password must be at least 6 symbols")
    public String password;
}
