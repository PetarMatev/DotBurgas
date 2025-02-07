package dotburgas.tracking.service;

import dotburgas.web.dto.PaymentNotificationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Service
public class TrackingService {

    @EventListener
    @Async
    public void trackNewPayment(PaymentNotificationEvent event) {

        System.out.printf("New payment for user [%s] happened.\n", event.getUserId());
    }
}
