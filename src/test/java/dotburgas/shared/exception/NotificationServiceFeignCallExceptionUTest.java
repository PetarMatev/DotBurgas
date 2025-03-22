package dotburgas.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
public class NotificationServiceFeignCallExceptionUTest {

    @Test
    void testExceptionWithoutMessage() {
        // Given
        NotificationServiceFeignCallException exception = new NotificationServiceFeignCallException();

        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionWithMessage() {
        // Given
        String errorMessage = "An error occurred while calling the notification service";
        NotificationServiceFeignCallException exception = new NotificationServiceFeignCallException(errorMessage);

        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testExceptionStackTrace() {
        // Given
        String errorMessage = "Error during Feign call";
        NotificationServiceFeignCallException exception = new NotificationServiceFeignCallException(errorMessage);

        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }
}

