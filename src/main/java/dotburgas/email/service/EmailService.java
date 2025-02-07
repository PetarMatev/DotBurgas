package dotburgas.email.service;

import dotburgas.web.dto.PaymentNotificationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Async
    @EventListener
    public void sendEmailWhenChargeHappen(PaymentNotificationEvent event) {

        System.out.printf("Charge happened for user with id [%s]. \n", event.getUserId());
    }
}
