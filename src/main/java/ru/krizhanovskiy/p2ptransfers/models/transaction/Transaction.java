package ru.krizhanovskiy.p2ptransfers.models.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private Long id;
    private Long sourceAccountId;
    private Long recipientAccountId;
    private Long amount;
    private TransactionType type;
    private TransactionStatus status;
    private LocalDateTime createdAt;

    public Transaction(Long sourceAccountId, Long recipientAccountId, Long amount, TransactionType type) {
        this.sourceAccountId = sourceAccountId;
        this.recipientAccountId = recipientAccountId;
        this.amount = amount;
        this.type = type;
        this.status = TransactionStatus.PENDING;
    }
}