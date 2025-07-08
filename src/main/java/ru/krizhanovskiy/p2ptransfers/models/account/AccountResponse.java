package ru.krizhanovskiy.p2ptransfers.models.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class AccountResponse {
    Long id;
    String name;
    String accountNumber;
    AccountStatus status;
    LocalDateTime createdAt;
    long balance;

    public AccountResponse(Account account, long balance) {
        this.id = account.getId();
        this.name = account.getName();
        this.accountNumber = account.getAccountNumber();
        this.status = account.getStatus();
        this.createdAt = account.getCreatedAt();
        this.balance = balance;
    }
}