package dotburgas.transaction;

import dotburgas.notification.service.NotificationService;
import dotburgas.transaction.repository.TransactionRepository;
import dotburgas.transaction.service.TransactionService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceUTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TransactionService transactionService;


}
