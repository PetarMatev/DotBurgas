package dotburgas.transaction;

import dotburgas.notification.service.NotificationService;
import dotburgas.shared.exception.DomainException;
import dotburgas.transaction.model.Transaction;
import dotburgas.transaction.model.TransactionStatus;
import dotburgas.transaction.model.TransactionType;
import dotburgas.transaction.repository.TransactionRepository;
import dotburgas.transaction.service.TransactionService;
import dotburgas.user.model.User;
import dotburgas.wallet.model.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
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

    // 02. getAllByOwnerId

    @Test
    void givenOwnerIdThatExistInTheDatabase_ThenReturnListOfTransactions() {

        UUID ownerId = UUID.randomUUID();
        List<Transaction> transactionList = List.of(Transaction.builder().build(), Transaction.builder().build());

        when(transactionRepository.findAllByOwnerIdOrderByCreatedOnDesc(ownerId)).thenReturn(transactionList);

        // When
        List<Transaction> returnedTransactionList = transactionService.getAllByOwnerId(ownerId);

        // Then
        assertEquals(transactionList.size(), returnedTransactionList.size());
        verify(transactionRepository, times(1)).findAllByOwnerIdOrderByCreatedOnDesc(ownerId);
    }

    // 03. getLastFiveTransactionsByWallet
    @Test
    void givenWalletThatExistInTheDatabase_thenReturnListOfLastFiveTransactions() {

        // Given
        User user = User.builder().id(UUID.randomUUID()).username("ivancho").build();

        UUID walletId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.ONE;

        Wallet wallet = Wallet.builder()
                .id(walletId)
                .balance(balance)
                .owner(user)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .currency(Currency.getInstance("EUR"))
                .build();

        Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .sender(wallet.getId().toString())
                .receiver(wallet.getId().toString())
                .amount(BigDecimal.valueOf(100.00))
                .balanceLeft(BigDecimal.valueOf(900.00))
                .currency(Currency.getInstance("EUR"))
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCEEDED)
                .description("Payment for invoice #1234")
                .failureReason(null)
                .createdOn(LocalDateTime.now())
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .sender(wallet.getId().toString())
                .receiver(wallet.getId().toString())
                .amount(BigDecimal.valueOf(50.00))
                .balanceLeft(BigDecimal.valueOf(850.00))
                .currency(Currency.getInstance("EUR"))
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCEEDED)
                .description("Transfer to savings account")
                .failureReason(null)
                .createdOn(LocalDateTime.now().minusDays(1))
                .build();


        List<Transaction> transactionList = List.of(transaction1, transaction2);


        when(transactionRepository.findAllBySenderOrReceiverOrderByCreatedOnDesc(wallet.getId().toString(), wallet.getId().toString())).thenReturn(transactionList);

        // When
        List<Transaction> returnedLastFiveTransactionsByWallet = transactionService.getLastFiveTransactionsByWallet(wallet);

        // Then
        assertEquals(transactionList.size(), returnedLastFiveTransactionsByWallet.size());
        verify(transactionRepository, times(1)).findAllBySenderOrReceiverOrderByCreatedOnDesc(wallet.getId().toString(), wallet.getId().toString());
        verifyNoMoreInteractions(transactionRepository);
    }

    // 04. createNewTransaction
    @Test
    void givenAllNecessaryFieldsForTransaction_thenReturnNewlyCreatedTransaction() {

        // Given

        // Given
        User user = User.builder().id(UUID.randomUUID()).username("ivancho").build();

        UUID walletId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.ONE;

        Wallet wallet = Wallet.builder()
                .id(walletId)
                .balance(balance)
                .owner(user)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .currency(Currency.getInstance("EUR"))
                .build();

        Transaction givenTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .sender(wallet.getId().toString())
                .receiver(wallet.getId().toString())
                .amount(BigDecimal.valueOf(50.00))
                .balanceLeft(BigDecimal.valueOf(850.00))
                .currency(Currency.getInstance("EUR"))
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCEEDED)
                .description("Transfer to savings account")
                .failureReason(null)
                .createdOn(LocalDateTime.now().minusDays(1))
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(givenTransaction);

        // When
        Transaction returnedTransaction = transactionService
                .createNewTransaction(user, "Ivan", "Petkan", BigDecimal.ONE, BigDecimal.TWO, Currency.getInstance("EUR"), TransactionType.DEPOSIT, TransactionStatus.SUCCEEDED, "description", null);
        // Then
        assertEquals(givenTransaction.getOwner().getUsername(), returnedTransaction.getOwner().getUsername());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

}
