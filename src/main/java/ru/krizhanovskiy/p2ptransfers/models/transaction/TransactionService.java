package ru.krizhanovskiy.p2ptransfers.models.transaction;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.krizhanovskiy.p2ptransfers.annotations.Timed;
import ru.krizhanovskiy.p2ptransfers.models.account.Account;
import ru.krizhanovskiy.p2ptransfers.models.account.AccountRepository;
import ru.krizhanovskiy.p2ptransfers.models.account.AccountStatus;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Timed
    public Transaction createTransaction(TransactionCreateRequest request) {
        if (request.sourceAccountNumber().equals(request.recipientAccountNumber()))
            throw new IllegalStateException("Счёта одинаковые");
        Account sourceAccount = accountRepository.findByAccountNumber(request.sourceAccountNumber());
        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) throw new IllegalStateException("Счёт отправителя закрыт");
        Account recipientAccount = accountRepository.findByAccountNumber(request.recipientAccountNumber());
        if (recipientAccount.getStatus() != AccountStatus.ACTIVE) throw new IllegalStateException("Счёт получателя закрыт");

        long balance = transactionRepository.getAccountBalance(sourceAccount.getId());
        if (balance < request.amount()) throw new IllegalStateException("На счёте не достаточно средств");

        Transaction transaction = new Transaction(sourceAccount.getId(), recipientAccount.getId(), request.amount(),
                TransactionType.TRANSFER);
        Transaction saved = transactionRepository.save(transaction);
        log.info("Транзакция ({} -> {}, сумма {}) создана с id={}",
                request.sourceAccountNumber(), request.recipientAccountNumber(), request.amount(), saved.getId());
        return saved;
    }

    @Timed
    public Transaction createInitialDeposit(Long accountId, long amount) {
        Transaction transaction = Transaction.builder()
                .recipientAccountId(accountId)
                .amount(amount)
                .type(TransactionType.INITIAL_DEPOSIT)
                .status(TransactionStatus.COMPLETED)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Первоначальный депозит {} добавлен к аккаунту с id={}", amount, accountId);
        return saved;
    }

    @Timed
    public Transaction confirmTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id);
        if (!transaction.getStatus().equals(TransactionStatus.PENDING)) {
            throw new IllegalStateException("Транзакция не в состоянии PENDING");
        }

        Long sourceBalance = transactionRepository.getAccountBalance(transaction.getSourceAccountId());
        if (sourceBalance < transaction.getAmount()) {
            throw new IllegalStateException("Недостаточно средств на счёте");
        }

        transactionRepository.updateStatus(id, TransactionStatus.COMPLETED);
        log.info("Транзакция с id={} подтверждена", id);
        return transaction;
    }

    @Timed
    public Transaction cancelTransaction(Long id, long userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId);
        if (!transaction.getStatus().equals(TransactionStatus.PENDING)) {
            throw new IllegalStateException("Транзакция не в состоянии PENDING");
        }
        transactionRepository.updateStatus(id, TransactionStatus.CANCELLED);
        log.info("Транзакция с id={} отменена", id);
        return transaction;
    }

    @Timed
    public Long getAccountBalance(Long accountId) {
        return transactionRepository.getAccountBalance(accountId);
    }
}