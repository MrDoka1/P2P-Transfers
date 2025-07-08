package ru.krizhanovskiy.p2ptransfers.models.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.krizhanovskiy.p2ptransfers.models.transaction.TransactionService;
import ru.krizhanovskiy.p2ptransfers.models.user.User;
import ru.krizhanovskiy.p2ptransfers.models.user.UserRepository;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AccountController accountController;

    private User user;
    private Account account;
    private AccountCreateRequest createRequest;
    private Principal principal;

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
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = new AccountCreateRequest("Test Account", 10000L);

        principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test@example.com");
    }

    @Test
    void createAccount_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(accountService.createAccount(1L, createRequest)).thenReturn(account);

        ResponseEntity<?> response = accountController.createAccount(principal, createRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(URI.create("/accounts/number/40817810123456789012"), response.getHeaders().getLocation());
        verify(accountService).createAccount(1L, createRequest);
    }

    @Test
    void getAccountByAccountNumber_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(accountService.findByAccountNumberAndUserId("40817810123456789012", 1L)).thenReturn(account);
        when(transactionService.getAccountBalance(1L)).thenReturn(10000L);

        ResponseEntity<AccountResponse> response = accountController.getAccountByAccountNumber(principal, "40817810123456789012");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AccountResponse result = response.getBody();
        assertNotNull(result);
        assertEquals("40817810123456789012", result.getAccountNumber());
        assertEquals(10000L, result.getBalance());
        verify(accountService).findByAccountNumberAndUserId("40817810123456789012", 1L);
        verify(transactionService).getAccountBalance(1L);
    }

    @Test
    void getAllAccounts_success() {
        AccountResponse response = AccountResponse.builder()
                .id(1L)
                .name("Test Account")
                .accountNumber("40817810123456789012")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .balance(10000L)
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(accountService.findByUserIdReturnAccountResponse(1L)).thenReturn(List.of(response));

        ResponseEntity<List<AccountResponse>> responseEntity = accountController.getAllAccounts(principal);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        List<AccountResponse> result = responseEntity.getBody();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("40817810123456789012", result.get(0).getAccountNumber());
        verify(accountService).findByUserIdReturnAccountResponse(1L);
    }

    @Test
    void closeAccount_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        doNothing().when(accountService).closeAccount(1L, "40817810123456789012");

        ResponseEntity<Void> response = accountController.closeAccount(principal, "40817810123456789012");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(accountService).closeAccount(1L, "40817810123456789012");
    }

    @Test
    void getFullNameByAccountNumber_success() {
        when(accountService.getFullNameByAccountNumber("40817810123456789012")).thenReturn("John Middle D.");

        ResponseEntity<AccountController.FullNameResponse> response = accountController.getFullNameByAccountNumber("40817810123456789012");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Middle D.", response.getBody().fio());
        verify(accountService).getFullNameByAccountNumber("40817810123456789012");
    }
}