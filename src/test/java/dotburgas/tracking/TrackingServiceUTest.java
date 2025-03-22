package dotburgas.tracking;

import dotburgas.web.dto.PaymentNotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrackingServiceUTest {

    @Mock
    private MailSender mailSender;

    @InjectMocks
    private TrackingService trackingService;

    @Test
    void trackNewPayment_ShouldLogPaymentProcessed() {
        PaymentNotificationEvent event = PaymentNotificationEvent.builder()
                .userId(UUID.randomUUID())
                .email("test@example.com")
                .amount(BigDecimal.ONE)
                .paymentTime(LocalDateTime.now())
                .build();
        trackingService.trackNewPayment(event);
        assertDoesNotThrow(() -> trackingService.trackNewPayment(event));
    }

    @Test
    void sendEmailWhenChargeHappen_ShouldSendEmailSuccessfully() {
        PaymentNotificationEvent event = PaymentNotificationEvent.builder()
                .userId(UUID.randomUUID())
                .email("test@example.com")
                .amount(BigDecimal.ONE)
                .paymentTime(LocalDateTime.now())
                .build();

        trackingService.sendEmailWhenChargeHappen(event);

        ArgumentCaptor<SimpleMailMessage> mailMessageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(mailMessageCaptor.capture());

        SimpleMailMessage sentMessage = mailMessageCaptor.getValue();
        assertNotNull(sentMessage);
        assertEquals("test@example.com", Objects.requireNonNull(sentMessage.getTo())[0]);
        assertEquals("Charge has been processed.", sentMessage.getSubject());
        assertTrue(Objects.requireNonNull(sentMessage.getText()).contains("A new charge has been applied to your card."));
    }

    @Test
    void sendEmailWhenChargeHappen_ShouldSkipWhenEmailIsBlank() {
        // Given an empty email
        PaymentNotificationEvent event = PaymentNotificationEvent.builder()
                .userId(UUID.randomUUID())
                .email("") // Blank email
                .amount(BigDecimal.ONE)
                .paymentTime(LocalDateTime.now())
                .build();

        // When
        trackingService.sendEmailWhenChargeHappen(event);

        // Then
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmailWhenChargeHappen_ShouldSkipWhenEmailIsNull() {
        // Given a null email
        PaymentNotificationEvent event = PaymentNotificationEvent.builder()
                .userId(UUID.randomUUID())
                .email(null) // Null email
                .amount(BigDecimal.ONE)
                .paymentTime(LocalDateTime.now())
                .build();

        // When
        trackingService.sendEmailWhenChargeHappen(event);

        // Then
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmailWhenChargeHappen_ShouldHandleMailSendingException() {
        PaymentNotificationEvent event = PaymentNotificationEvent.builder()
                .userId(UUID.randomUUID())
                .email("test@example.com")
                .amount(BigDecimal.ONE)
                .paymentTime(LocalDateTime.now())
                .build();

        doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> trackingService.sendEmailWhenChargeHappen(event));

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
