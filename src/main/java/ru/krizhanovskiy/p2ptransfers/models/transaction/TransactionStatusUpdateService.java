package ru.krizhanovskiy.p2ptransfers.models.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransactionStatusUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionStatusUpdateService.class);
    private final JdbcTemplate jdbcTemplate;

    public TransactionStatusUpdateService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedRate = 60000) // 1 минута
    public void updatePendingTransactionsToFailed() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusMinutes(5);
        String sql = "UPDATE transactions SET status = ? WHERE status = ? AND created_at < ?";

        int updatedRows = jdbcTemplate.update(
                sql,
                TransactionStatus.FAILED.name(),
                TransactionStatus.PENDING.name(),
                java.sql.Timestamp.valueOf(threshold)
        );

        if (updatedRows > 0) {
            logger.info("Updated {} transactions from PENDING to FAILED", updatedRows);
        } else {
            logger.info("No transactions found to update from PENDING to FAILED");
        }
    }
}