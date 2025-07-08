package ru.krizhanovskiy.p2ptransfers.models.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.krizhanovskiy.p2ptransfers.models.account.Account;
import ru.krizhanovskiy.p2ptransfers.models.account.AccountRepository;
import ru.krizhanovskiy.p2ptransfers.models.account.AccountStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;
    private TransactionService transactionService;

    @BeforeEach
    void setup() {
        transactionRepository = mock(TransactionRepository.class);
        accountRepository = mock(AccountRepository.class);
        transactionService = new TransactionService(transactionRepository, accountRepository);
    }

    @Test
    void createTransaction_shouldCreateSuccessfully_whenDataIsValid() {
        String source = "12345678901234567890";
        String recipient = "09876543210987654321";
        long amount = 1000L;

        Account sourceAcc = new Account(1L, 1L, "Acc1", source, AccountStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
        Account recipientAcc = new Account(2L, 1L, "Acc2", recipient, AccountStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());

        when(accountRepository.findByAccountNumber(source)).thenReturn(sourceAcc);
        when(accountRepository.findByAccountNumber(recipient)).thenReturn(recipientAcc);
        when(transactionRepository.getAccountBalance(1L)).thenReturn(2000L);

        Transaction saved = Transaction.builder()
                .id(1L)
                .sourceAccountId(1L)
                .recipientAccountId(2L)
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .build();

        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionCreateRequest request = new TransactionCreateRequest(source, recipient, amount);
        Transaction result = transactionService.createTransaction(request);

        assertNotNull(result);
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        verify(transactionRepository).save(any());
    }

    @Test
    void confirmTransaction_shouldThrow_ifNotPending() {
        Transaction transaction = Transaction.builder()
                .id(1L)
                .sourceAccountId(1L)
                .recipientAccountId(2L)
                .amount(1000L)
                .status(TransactionStatus.COMPLETED)
                .type(TransactionType.TRANSFER)
                .build();

        when(transactionRepository.findById(1L)).thenReturn(transaction);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                transactionService.confirmTransaction(1L));

        assertTrue(ex.getMessage().contains("не в состоянии PENDING"));
    }

    @Test
    void cancelTransaction_shouldUpdateStatus_ifPending() {
        Transaction tx = Transaction.builder()
                .id(1L)
                .sourceAccountId(1L)
                .recipientAccountId(2L)
                .status(TransactionStatus.PENDING)
                .build();

        when(transactionRepository.findByIdAndUserId(1L, 42L)).thenReturn(tx);

        Transaction result = transactionService.cancelTransaction(1L, 42L);

        verify(transactionRepository).updateStatus(1L, TransactionStatus.CANCELLED);
        assertEquals(TransactionStatus.PENDING, result.getStatus());
    }
}
