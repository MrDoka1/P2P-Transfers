package ru.krizhanovskiy.p2ptransfers.models.account;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Account {
    private Long id;
    private Long userId;
    private String name;
    private Long accountNumber;
    private String status;
    private LocalDateTime createdAt;

    public Account() {}

    public Account(Long userId, String name, Long accountNumber, String status) {
        this.userId = userId;
        this.name = name;
        this.accountNumber = accountNumber;
        this.status = status;
    }
}