package ru.krizhanovskiy.p2ptransfers.models.transaction;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.krizhanovskiy.p2ptransfers.exceptions.TransactionNotFoundException;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Objects;

@Repository
public class TransactionRepository {
    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Transaction> transactionRowMapper = (rs, rowNum) -> Transaction.builder()
            .id(rs.getLong("id"))
            .sourceAccountId(rs.getLong("source_account_id"))
            .recipientAccountId(rs.getLong("recipient_account_id"))
            .amount(rs.getLong("amount"))
            .type(TransactionType.valueOf(rs.getString("type")))
            .status(TransactionStatus.valueOf(rs.getString("status")))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    public Transaction save(Transaction transaction) {
        String sql = "INSERT INTO transactions (source_account_id, recipient_account_id, amount, type, status, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});

            if (transaction.getType() == TransactionType.INITIAL_DEPOSIT) ps.setNull(1, Types.BIGINT);
            else ps.setLong(1, transaction.getSourceAccountId());

            ps.setLong(2, transaction.getRecipientAccountId());
            ps.setLong(3, transaction.getAmount());
            ps.setString(4, transaction.getType().name());
            ps.setString(5, transaction.getStatus().name());
            ps.setTimestamp(6, java.sql.Timestamp.valueOf(now));
            return ps;
        }, keyHolder);
        return Transaction.builder()
                .id(Objects.requireNonNull(keyHolder.getKey()).longValue())
                .sourceAccountId(transaction.getSourceAccountId())
                .recipientAccountId(transaction.getRecipientAccountId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .createdAt(now)
                .build();
    }

    public Transaction findById(Long id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, transactionRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new TransactionNotFoundException("Транзакция с ID " + id + " не найдена");
        }
    }

    public Transaction findByIdAndUserId(Long id, Long userId) {
        String sql = """
            SELECT t.* FROM transactions t 
            JOIN accounts a ON t.source_account_id = a.id 
               WHERE t.id = ? AND a.user_id = ?
        """;
        try {
            return jdbcTemplate.queryForObject(sql, transactionRowMapper, id, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new TransactionNotFoundException("Транзакция с ID " + id + " для пользователя с ID " + userId + " не найдена");
        }
    }

    public void updateStatus(Long id, TransactionStatus status) {
        String sql = "UPDATE transactions SET status = ?, created_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), java.sql.Timestamp.valueOf(LocalDateTime.now()), id);
    }

    public Long getAccountBalance(Long accountId) {
        String sql = """
            SELECT COALESCE((
                SELECT SUM(amount) 
                FROM transactions 
                WHERE recipient_account_id = ? AND status = 'COMPLETED'
            ) - (
                SELECT COALESCE(SUM(amount), 0)
                FROM transactions 
                WHERE source_account_id = ? AND status = 'COMPLETED'
            ), 0) AS balance
        """;
        return jdbcTemplate.queryForObject(sql, Long.class, accountId, accountId);
    }
}