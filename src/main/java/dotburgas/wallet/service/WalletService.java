package dotburgas.wallet.service;

import dotburgas.shared.exception.DomainException;
import dotburgas.transaction.model.Transaction;
import dotburgas.transaction.model.TransactionStatus;
import dotburgas.transaction.model.TransactionType;
import dotburgas.transaction.service.TransactionService;
import dotburgas.user.model.User;
import dotburgas.wallet.model.Wallet;
import dotburgas.wallet.repository.WalletRepository;
import dotburgas.web.dto.TransferRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class WalletService {

    private static final String DOT_BURGAS_LTD = "Dot Burgas Wallet Ltd";

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;

    @Autowired
    public WalletService(WalletRepository walletRepository, TransactionService transactionService) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
    }

    public Wallet createNewWallet(User user) {
        Wallet wallet = walletRepository.save(initializeWallet(user));
        log.info("Successfully created new wallet with id [%s] and balance [%.2f]".formatted(wallet.getId(), wallet.getBalance()));
        return wallet;
    }

    @Transactional
    public Transaction topUp(UUID walletId, BigDecimal amount) {

        // if error throws, else returns wallet.
        Wallet wallet = getWalletByID(walletId);
        String transactionDescription = "Top up %.2f".formatted(amount.doubleValue());

        Transaction transaction = transactionService
                .createNewTransaction(
                        wallet.getOwner(),
                        DOT_BURGAS_LTD,
                        walletId.toString(),
                        amount,
                        wallet.getBalance(),
                        wallet.getCurrency(),
                        TransactionType.DEPOSIT,
                        TransactionStatus.SUCCEEDED,
                        transactionDescription,
                        null);

        wallet.setBalance(wallet.getBalance().add(amount));
        // when I update state on Entity need to also update state of "updatedOn"
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);

        return transaction;
    }

    public Transaction transferFunds(User sender, @Valid TransferRequest transferRequest) {

        Wallet senderWallet = getWalletByID(transferRequest.getFromWalletId());

        Optional<Wallet> receiverWalletOptional = walletRepository.findAllByOwnerUsername(transferRequest.getToUserName())
                .stream()
                .findFirst();

        String transferDescription = "Transfer from to %s %s for %.2f".formatted(sender.getUsername(), transferRequest.getToUserName(), transferRequest.getAmount());

        if (receiverWalletOptional.isEmpty()) {
            return transactionService
                    .createNewTransaction(
                            sender,
                            senderWallet.getId().toString(),
                            transferRequest.getToUserName(),
                            transferRequest.getAmount(),
                            senderWallet.getBalance(),
                            senderWallet.getCurrency(),
                            TransactionType.WITHDRAWAL,
                            TransactionStatus.FAILED,
                            transferDescription,
                            "Invalid criteria for transfer");
        }

        // Money Transfer
        // Ivan -> Gosho | 20 EUR
        // Ivan -20.00 EUR
        // Gosho +20.00 EUR

        Transaction withdrawal = charge(sender, senderWallet.getId(), transferRequest.getAmount(), transferDescription);
        if (withdrawal.getStatus() == TransactionStatus.FAILED) {
            return withdrawal;
        }

        Wallet receiverWallet = receiverWalletOptional.get();
        receiverWallet.setBalance(receiverWallet.getBalance().add(transferRequest.getAmount()));
        receiverWallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(receiverWallet);

        transactionService
                .createNewTransaction(
                        receiverWallet.getOwner(),
                        senderWallet.getId().toString(),
                        receiverWallet.getId().toString(),
                        transferRequest.getAmount(),
                        receiverWallet.getBalance(),
                        receiverWallet.getCurrency(),
                        TransactionType.DEPOSIT,
                        TransactionStatus.SUCCEEDED,
                        transferDescription,
                        null);

        return withdrawal;
    }

    @Transactional
    public Transaction charge(User user, UUID walletId, BigDecimal amount, String description) {

        Wallet wallet = getWalletByID(walletId);

        String failureReason = "Insufficient balance";
        // validation if the current balance is less than the amount
        if (wallet.getBalance().compareTo(amount) < 0) {
            return transactionService.createNewTransaction(
                    user,
                    wallet.getId().toString(),
                    DOT_BURGAS_LTD,
                    amount,
                    wallet.getBalance(),
                    wallet.getCurrency(),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    description,
                    failureReason
            );

        }

        // if the user has sufficient funds, we subtract the amount.
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);

        return transactionService.createNewTransaction(user,
                wallet.getId().toString(),
                DOT_BURGAS_LTD,
                amount,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCEEDED,
                description,
                null);
    }

    private Wallet getWalletByID(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new DomainException("Wallet with id [%s] does not exists".formatted(walletId)));
    }

    private Wallet initializeWallet(User user) {
        return Wallet.builder()
                .owner(user)
                .balance(new BigDecimal("1000.00"))
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }
}
