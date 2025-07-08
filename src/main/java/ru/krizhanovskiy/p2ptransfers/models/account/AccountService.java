package ru.krizhanovskiy.p2ptransfers.models.account;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.krizhanovskiy.p2ptransfers.annotations.Timed;
import ru.krizhanovskiy.p2ptransfers.exceptions.AccountNotFoundException;
import ru.krizhanovskiy.p2ptransfers.models.transaction.TransactionService;
import ru.krizhanovskiy.p2ptransfers.models.user.User;
import ru.krizhanovskiy.p2ptransfers.models.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    @Lazy
    private final TransactionService transactionService;

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    @Timed
    @Transactional
    public Account createAccount(long userId, AccountCreateRequest request) {
        String accountNumber;
        do {
            accountNumber = accountNumberGenerator.generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        Account account = Account.builder()
                .userId(userId)
                .name(request.name())
                .accountNumber(accountNumber)
                .status(AccountStatus.ACTIVE)
                .build();
        Account accountSaved = accountRepository.save(account);
        if (request.balance() > 0) {
            transactionService.createInitialDeposit(accountSaved.getId(), request.balance());
        }
        log.info("Аккаунт создан с номером {}", accountNumber);
        return accountSaved;
    }

    @Timed
    public Account findByAccountNumber(String accountNumber) throws AccountNotFoundException {
        return accountRepository.findByAccountNumber(accountNumber);
    }
    @Timed
    public Account findByAccountNumberAndUserId(String accountNumber, long userId) throws AccountNotFoundException {
        return accountRepository.findByAccountNumberAndUserId(accountNumber, userId);
    }

    @Timed
    public Account findById(Long id) throws AccountNotFoundException {
        return accountRepository.findById(id);
    }

    @Timed
    public Account findByIdAndUserId(long id, long userId) throws AccountNotFoundException {
        return accountRepository.findByIdAndUserId(id, userId);
    }

    @Timed
    public List<Account> findByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    @Timed
    public List<AccountResponse> findByUserIdReturnAccountResponse(Long userId) throws AccountNotFoundException {
        return accountRepository.findByUserIdWithBalance(userId);
    }

    @Timed
    public void closeAccount(long userId, String accountNumber) throws AccountNotFoundException {
        long accountId = accountRepository.getIdByUserIdAndAccountNumber(userId, accountNumber);
        accountRepository.closeAccountById(accountId);
        log.info("Аккаунт с id={} успешно закрыт", accountId);
    }

    @Timed
    public String getFullNameByAccountNumber(String accountNumber) throws AccountNotFoundException {
        Account account = accountRepository.findByAccountNumber(accountNumber);

        Long userId = account.getUserId();
        User user = userRepository.findById(userId);

        StringBuilder fullName = new StringBuilder();
        fullName
                .append(user.getFirstName() != null ? user.getFirstName() : "")
                .append(" ")
                .append(user.getMiddleName() != null ? user.getMiddleName() : "");
        if (user.getLastName() != null) fullName
                .append(' ')
                .append(user.getLastName().charAt(0))
                .append('.');
        return fullName.toString().trim();
    }
}