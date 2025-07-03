package ru.krizhanovskiy.p2ptransfers.models.transaction;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Transaction {
    private Long id;
    private Long sourceAccountId;
    private Long recipientAccountId;
    private Long amount;
    private String type;
    private String status;
    private LocalDateTime createdAt;

    public Transaction() {}

    public Transaction(Long sourceAccountId, Long recipientAccountId, Long amount, String type, String status) {
        this.sourceAccountId = sourceAccountId;
        this.recipientAccountId = recipientAccountId;
        this.amount = amount;
        this.type = type;
        this.status = status;
    }
}