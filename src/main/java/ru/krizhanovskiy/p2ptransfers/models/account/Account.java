package ru.krizhanovskiy.p2ptransfers.models.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private Long id;
    private Long userId;
    private String name;
    private String accountNumber;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Account(Long userId, String name, String accountNumber) {
        this.userId = userId;
        this.name = name;
        this.accountNumber = accountNumber;
        this.status = AccountStatus.ACTIVE;
    }
}