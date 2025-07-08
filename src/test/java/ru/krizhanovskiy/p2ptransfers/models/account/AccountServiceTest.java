package ru.krizhanovskiy.p2ptransfers.models.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.krizhanovskiy.p2ptransfers.exceptions.AccountNotFoundException;
import ru.krizhanovskiy.p2ptransfers.models.transaction.TransactionService;
import ru.krizhanovskiy.p2ptransfers.models.user.User;
import ru.krizhanovskiy.p2ptransfers.models.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AccountService accountService;

    private User user;
    private Account account;
    private AccountCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .middleName("Middle")
                .build();

        account = Account.builder()
                .id(1L)
                .userId(1L)
                .name("Test Account")
                .accountNumber("40817810123456789012")
                .status(AccountStatus.ACTIVE)
                .build();

        createRequest = new AccountCreateRequest("Test Account", 0L);
    }

    @Test
    void createAccount_success_noInitialBalance() {
        when(accountNumberGenerator.generateAccountNumber()).thenReturn("40817810123456789012");
        when(accountRepository.existsByAccountNumber("40817810123456789012")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.createAccount(1L, createRequest);

        assertNotNull(result);
        assertEquals("Test Account", result.getName());
        assertEquals("40817810123456789012", result.getAccountNumber());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        verify(accountRepository).save(any(Account.class));
        verify(transactionService, never()).createInitialDeposit(anyLong(), anyLong());
    }

    @Test
    void createAccount_success_withInitialBalance() {
        when(accountNumberGenerator.generateAccountNumber()).thenReturn("40817810123456789012");
        when(accountRepository.existsByAccountNumber("40817810123456789012")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        createRequest = new AccountCreateRequest("Test Account", 5000L);

        Account result = accountService.createAccount(1L, createRequest);

        assertNotNull(result);
        assertEquals("Test Account", result.getName());
        verify(transactionService).createInitialDeposit(account.getId(), 5000L);
    }

    @Test
    void findByAccountNumber_success() {
        when(accountRepository.findByAccountNumber("40817810123456789012")).thenReturn(account);

        Account result = accountService.findByAccountNumber("40817810123456789012");

        assertNotNull(result);
        assertEquals(account.getAccountNumber(), result.getAccountNumber());
        verify(accountRepository).findByAccountNumber("40817810123456789012");
    }

    @Test
    void findByAccountNumber_notFound_throwsException() {
        when(accountRepository.findByAccountNumber("40817810123456789012")).thenThrow(new AccountNotFoundException());

        assertThrows(AccountNotFoundException.class, () -> accountService.findByAccountNumber("40817810123456789012"));
        verify(accountRepository).findByAccountNumber("40817810123456789012");
    }

    @Test
    void findByAccountNumberAndUserId_success() {
        when(accountRepository.findByAccountNumberAndUserId("40817810123456789012", 1L)).thenReturn(account);

        Account result = accountService.findByAccountNumberAndUserId("40817810123456789012", 1L);

        assertNotNull(result);
        assertEquals(account.getAccountNumber(), result.getAccountNumber());
        verify(accountRepository).findByAccountNumberAndUserId("40817810123456789012", 1L);
    }

    @Test
    void findById_success() {
        when(accountRepository.findById(1L)).thenReturn(account);

        Account result = accountService.findById(1L);

        assertNotNull(result);
        assertEquals(account.getId(), result.getId());
        verify(accountRepository).findById(1L);
    }

    @Test
    void findByUserId_success() {
        when(accountRepository.findByUserId(1L)).thenReturn(List.of(account));

        List<Account> result = accountService.findByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(account.getId(), result.get(0).getId());
        verify(accountRepository).findByUserId(1L);
    }

    @Test
    void findByUserIdReturnAccountResponse_success() {
        AccountResponse response = AccountResponse.builder()
                .id(1L)
                .name("Test Account")
                .accountNumber("40817810123456789012")
                .status(AccountStatus.ACTIVE)
                .balance(10000L)
                .build();
        when(accountRepository.findByUserIdWithBalance(1L)).thenReturn(List.of(response));

        List<AccountResponse> result = accountService.findByUserIdReturnAccountResponse(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(response.getAccountNumber(), result.get(0).getAccountNumber());
        verify(accountRepository).findByUserIdWithBalance(1L);
    }

    @Test
    void closeAccount_success() {
        when(accountRepository.getIdByUserIdAndAccountNumber(1L, "40817810123456789012")).thenReturn(1L);
        doNothing().when(accountRepository).closeAccountById(1L);

        accountService.closeAccount(1L, "40817810123456789012");

        verify(accountRepository).getIdByUserIdAndAccountNumber(1L, "40817810123456789012");
        verify(accountRepository).closeAccountById(1L);
    }

    @Test
    void getFullNameByAccountNumber_success() {
        when(accountRepository.findByAccountNumber("40817810123456789012")).thenReturn(account);
        when(userRepository.findById(1L)).thenReturn(user);

        String result = accountService.getFullNameByAccountNumber("40817810123456789012");

        assertEquals("John Middle D.", result);
        verify(accountRepository).findByAccountNumber("40817810123456789012");
        verify(userRepository).findById(1L);
    }
}