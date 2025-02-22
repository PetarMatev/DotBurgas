package dotburgas.transaction.repository;

import dotburgas.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findAllByOwnerIdOrderByCreatedOnDesc(UUID ownerId);

    List<Transaction> findAllBySenderOrReceiverOrderByCreatedOnDesc(String sender, String receiver);
}
