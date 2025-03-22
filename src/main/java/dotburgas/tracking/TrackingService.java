package dotburgas.tracking;

import dotburgas.web.dto.PaymentNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TrackingService {

    private final MailSender mailSender;

    @Autowired
    public TrackingService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @EventListener
    public void trackNewPayment(PaymentNotificationEvent event) {
        log.info("New payment for user with Id: [{}] has been processed.", event.getUserId());
    }

    @Async
    @EventListener
    public void sendEmailWhenChargeHappen(PaymentNotificationEvent event) {

        if (event.getEmail() == null || event.getEmail().isBlank()) {
            log.warn("Skipping email notification: No email address provided for user [{}].", event.getUserId());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.getEmail());
        message.setSubject("Charge has been processed.");

        String emailBody = """
            Hello,
            
            A new charge has been applied to your card.
            
            Please log in to your online banking and verify the transaction.
            
            Thank you!
            """;

        message.setText(emailBody);

        try {
            mailSender.send(message);
            log.info("Successfully sent payment charge warning email to [{}].", event.getEmail());
        } catch (Exception e) {
            log.warn("Failed to send email to [{}] due to error: {}.", event.getEmail(), e.getMessage());
        }
    }
}

