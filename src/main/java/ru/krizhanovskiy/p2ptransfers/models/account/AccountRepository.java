package ru.krizhanovskiy.p2ptransfers.models.account;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.krizhanovskiy.p2ptransfers.exceptions.AccountNotFoundException;


import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Repository
public class AccountRepository {
    private final JdbcTemplate jdbcTemplate;

    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Account> accountRowMapper = (rs, rowNum) -> Account.builder()
            .id(rs.getLong("id"))
            .userId(rs.getLong("user_id"))
            .name(rs.getString("name"))
            .accountNumber(rs.getString("account_number"))
            .status(AccountStatus.valueOf(rs.getString("status")))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    private final RowMapper<AccountResponse> accountResponseRowMapper = (rs, rowNum) -> AccountResponse.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .accountNumber(rs.getString("account_number"))
            .status(AccountStatus.valueOf(rs.getString("status")))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .balance(rs.getLong("balance"))
            .build();

    public Account save(Account account) {
        String sql = "INSERT INTO accounts (user_id, name, account_number, status, created_at) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, account.getUserId());
            ps.setString(2, account.getName());
            ps.setString(3, account.getAccountNumber());
            ps.setString(4, account.getStatus().name());
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(now));
            return ps;
        }, keyHolder);
        return Account.builder()
                .id(Objects.requireNonNull(keyHolder.getKey()).longValue())
                .userId(account.getUserId())
                .name(account.getName())
                .accountNumber(account.getAccountNumber())
                .status(account.getStatus())
                .createdAt(now)
                .build();
    }

    public Account findByAccountNumber(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try {
            return jdbcTemplate.queryForObject(sql, accountRowMapper, accountNumber);
        } catch (EmptyResultDataAccessException e) {
            throw new AccountNotFoundException();
        }
    }
    public Account findByAccountNumberAndUserId(String accountNumber, long userId) {
        String sql = "SELECT * FROM accounts WHERE account_number = ? and user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, accountRowMapper, accountNumber, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new AccountNotFoundException();
        }
    }

    public boolean existsByAccountNumber(String accountNumber) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE account_number = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, accountNumber);
        return count > 0;
    }

    public Account findById(long id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, accountRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new AccountNotFoundException();
        }
    }
    public Account findByIdAndUserId(long id, long userId) {
        String sql = "SELECT * FROM accounts WHERE id = ? and user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, accountRowMapper, id, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new AccountNotFoundException();
        }
    }

    public List<Account> findAll() {
        String sql = "SELECT * FROM accounts";
        return jdbcTemplate.query(sql, accountRowMapper);
    }

    public List<Account> findByUserId(long userId) {
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        return jdbcTemplate.query(sql, accountRowMapper, userId);
    }
    public List<AccountResponse> findByUserIdWithBalance(long userId) {
        String sql = "SELECT a.*, COALESCE(SUM(CASE WHEN t.recipient_account_id = a.id AND t.status = 'COMPLETED' THEN t.amount ELSE 0 END) - " +
                "SUM(CASE WHEN t.source_account_id = a.id AND t.status = 'COMPLETED' THEN t.amount ELSE 0 END), 0) AS balance " +
                "FROM accounts a " +
                "LEFT JOIN transactions t ON t.recipient_account_id = a.id OR t.source_account_id = a.id " +
                "WHERE a.user_id = ? " +
                "GROUP BY a.id, a.name, a.account_number, a.status, a.created_at";
        return jdbcTemplate.query(sql, accountResponseRowMapper, userId);
    }
    public List<AccountResponse> findByUserIdWithBalanceAndActive(long userId) {
        String sql = "SELECT a.*, COALESCE(SUM(CASE WHEN t.recipient_account_id = a.id AND t.status = 'COMPLETED' THEN t.amount ELSE 0 END) - " +
                "SUM(CASE WHEN t.source_account_id = a.id AND t.status = 'COMPLETED' THEN t.amount ELSE 0 END), 0) AS balance " +
                "FROM accounts a " +
                "LEFT JOIN transactions t ON t.recipient_account_id = a.id OR t.source_account_id = a.id " +
                "WHERE a.user_id = ? and a.status = ?" +
                "GROUP BY a.id, a.name, a.account_number, a.status, a.created_at";
        return jdbcTemplate.query(sql, accountResponseRowMapper, userId, AccountStatus.ACTIVE.name());
    }

    public long getIdByUserIdAndAccountNumber(long userId, String accountNumber) {
        String sql = "SELECT id FROM accounts WHERE user_id = ? AND account_number = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, userId, accountNumber);
        } catch (EmptyResultDataAccessException e) {
            throw new AccountNotFoundException();
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void closeAccountById(Long id) {
        String sql = "UPDATE accounts SET status = ?, updated_at = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                AccountStatus.CLOSED.name(),
                java.sql.Timestamp.valueOf(LocalDateTime.now()),
                id);
        if (rowsAffected == 0) {
            throw new AccountNotFoundException();
        }
    }
}