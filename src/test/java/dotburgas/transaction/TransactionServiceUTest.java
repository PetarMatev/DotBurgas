package dotburgas.transaction;

import dotburgas.notification.service.NotificationService;
import dotburgas.shared.exception.DomainException;
import dotburgas.transaction.model.Transaction;
import dotburgas.transaction.repository.TransactionRepository;
import dotburgas.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceUTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TransactionService transactionService;

    // Test 01. getById
    @Test
    void givenIdThatDoesNotExistInTheDatabase_thenThrowException() {

        // Given
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DomainException.class, () -> transactionService.getById(transactionId));
        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    void givenIdThatDoesExistInTheDatabase_thenReturnTransactionObject() {

        // Given
        UUID transactionId = UUID.randomUUID();
        Transaction expectedTransaction = Transaction.builder().id(transactionId).build();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(expectedTransaction));

        // When
        Transaction returnedTransaction = transactionService.getById(transactionId);

        // Then
        assertEquals(expectedTransaction.getId(), returnedTransaction.getId(), "The returned transaction ID should match the expected ID.");
        assertNotNull(returnedTransaction, "The returned transaction should not be null.");
        verify(transactionRepository, times(1)).findById(transactionId);
        verifyNoMoreInteractions(transactionRepository);
    }

    // 02.
}
