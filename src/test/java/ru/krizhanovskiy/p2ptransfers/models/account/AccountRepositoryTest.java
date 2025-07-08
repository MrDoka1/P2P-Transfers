package ru.krizhanovskiy.p2ptransfers.models.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import ru.krizhanovskiy.p2ptransfers.exceptions.AccountNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private AccountRepository accountRepository;

    private Account account;
    private AccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .userId(1L)
                .name("Test Account")
                .accountNumber("40817810123456789012")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        accountResponse = AccountResponse.builder()
                .id(1L)
                .name("Test Account")
                .accountNumber("40817810123456789012")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .balance(10000L)
                .build();
    }

    @Test
    void save_success() {
        KeyHolder keyHolder = mock(KeyHolder.class);
        when(keyHolder.getKey()).thenReturn(1L);
        when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class))).thenAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            kh.getKeyList().add(Map.of("id", 1L)); // Имитация добавления ключа
            return 1;
        });

        Account result = accountRepository.save(account);

        ArgumentCaptor<PreparedStatementCreator> pscCaptor = ArgumentCaptor.forClass(PreparedStatementCreator.class);
        ArgumentCaptor<KeyHolder> keyHolderCaptor = ArgumentCaptor.forClass(KeyHolder.class);
        verify(jdbcTemplate).update(pscCaptor.capture(), keyHolderCaptor.capture());

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(account.getAccountNumber(), result.getAccountNumber());
        assertNotNull(keyHolderCaptor.getValue());
    }

    @Test
    void findByAccountNumber_success() {
        when(jdbcTemplate.queryForObject(eq("SELECT * FROM accounts WHERE account_number = ?"), any(RowMapper.class), eq("40817810123456789012")))
                .thenReturn(account);

        Account result = accountRepository.findByAccountNumber("40817810123456789012");

        assertNotNull(result);
        assertEquals(account.getAccountNumber(), result.getAccountNumber());
        verify(jdbcTemplate).queryForObject(eq("SELECT * FROM accounts WHERE account_number = ?"), any(RowMapper.class), eq("40817810123456789012"));
    }

    @Test
    void findByAccountNumber_notFound_throwsException() {
        when(jdbcTemplate.queryForObject(eq("SELECT * FROM accounts WHERE account_number = ?"), any(RowMapper.class), eq("40817810123456789012")))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(AccountNotFoundException.class, () -> accountRepository.findByAccountNumber("40817810123456789012"));
        verify(jdbcTemplate).queryForObject(eq("SELECT * FROM accounts WHERE account_number = ?"), any(RowMapper.class), eq("40817810123456789012"));
    }

    @Test
    void findByAccountNumberAndUserId_success() {
        when(jdbcTemplate.queryForObject(eq("SELECT * FROM accounts WHERE account_number = ? and user_id = ?"), any(RowMapper.class), eq("40817810123456789012"), eq(1L)))
                .thenReturn(account);

        Account result = accountRepository.findByAccountNumberAndUserId("40817810123456789012", 1L);

        assertNotNull(result);
        assertEquals(account.getAccountNumber(), result.getAccountNumber());
        verify(jdbcTemplate).queryForObject(eq("SELECT * FROM accounts WHERE account_number = ? and user_id = ?"), any(RowMapper.class), eq("40817810123456789012"), eq(1L));
    }

    @Test
    void existsByAccountNumber_success() {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM accounts WHERE account_number = ?"), eq(Integer.class), eq("40817810123456789012")))
                .thenReturn(1);

        boolean result = accountRepository.existsByAccountNumber("40817810123456789012");

        assertTrue(result);
        verify(jdbcTemplate).queryForObject(eq("SELECT COUNT(*) FROM accounts WHERE account_number = ?"), eq(Integer.class), eq("40817810123456789012"));
    }

    @Test
    void findById_success() {
        when(jdbcTemplate.queryForObject(eq("SELECT * FROM accounts WHERE id = ?"), any(RowMapper.class), eq(1L)))
                .thenReturn(account);

        Account result = accountRepository.findById(1L);

        assertNotNull(result);
        assertEquals(account.getId(), result.getId());
        verify(jdbcTemplate).queryForObject(eq("SELECT * FROM accounts WHERE id = ?"), any(RowMapper.class), eq(1L));
    }

    @Test
    void findByUserId_success() {
        when(jdbcTemplate.query(eq("SELECT * FROM accounts WHERE user_id = ?"), any(RowMapper.class), eq(1L)))
                .thenReturn(List.of(account));

        List<Account> result = accountRepository.findByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(account.getId(), result.get(0).getId());
        verify(jdbcTemplate).query(eq("SELECT * FROM accounts WHERE user_id = ?"), any(RowMapper.class), eq(1L));
    }

    @Test
    void findByUserIdWithBalance_success() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1L)))
                .thenReturn(List.of(accountResponse));

        List<AccountResponse> result = accountRepository.findByUserIdWithBalance(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(accountResponse.getAccountNumber(), result.get(0).getAccountNumber());
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), eq(1L));
    }

    @Test
    void getIdByUserIdAndAccountNumber_success() {
        when(jdbcTemplate.queryForObject(eq("SELECT id FROM accounts WHERE user_id = ? AND account_number = ?"), eq(Long.class), eq(1L), eq("40817810123456789012")))
                .thenReturn(1L);

        long result = accountRepository.getIdByUserIdAndAccountNumber(1L, "40817810123456789012");

        assertEquals(1L, result);
        verify(jdbcTemplate).queryForObject(eq("SELECT id FROM accounts WHERE user_id = ? AND account_number = ?"), eq(Long.class), eq(1L), eq("40817810123456789012"));
    }

    @Test
    void closeAccountById_success() {
        when(jdbcTemplate.update(anyString(), eq(AccountStatus.CLOSED.name()), any(), eq(1L))).thenReturn(1);

        accountRepository.closeAccountById(1L);

        verify(jdbcTemplate).update(eq("UPDATE accounts SET status = ?, updated_at = ? WHERE id = ?"), eq(AccountStatus.CLOSED.name()), any(), eq(1L));
    }

    @Test
    void closeAccountById_notFound_throwsException() {
        when(jdbcTemplate.update(anyString(), eq(AccountStatus.CLOSED.name()), any(), eq(1L))).thenReturn(0);

        assertThrows(AccountNotFoundException.class, () -> accountRepository.closeAccountById(1L));
        verify(jdbcTemplate).update(eq("UPDATE accounts SET status = ?, updated_at = ? WHERE id = ?"), eq(AccountStatus.CLOSED.name()), any(), eq(1L));
    }
}