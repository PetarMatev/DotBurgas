package dotburgas.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class HelloWorldScheduler {

    // Scheduled Job
    // Cron Job - at least one for the project
    @Scheduled(fixedDelay = 10000)
    public void sayHelloEvery10Seconds() {

        System.out.println(LocalDateTime.now() + "Hello World");
    }
}
