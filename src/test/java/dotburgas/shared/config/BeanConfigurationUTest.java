package dotburgas.shared.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class BeanConfigurationUTest {

    @Test
    void testPasswordEncoderBean() {

        // Given
        ApplicationContext context = new AnnotationConfigApplicationContext(BeanConfiguration.class);

        // When
        PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);

        // Then
        assertNotNull(passwordEncoder, "PasswordEncoder bean should not be null");
        assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder, "PasswordEncoder bean should be an instance of BCryptPasswordEncoder");
    }
}