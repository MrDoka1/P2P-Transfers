package ru.krizhanovskiy.p2ptransfers.models.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.krizhanovskiy.p2ptransfers.exceptions.ErrorResponse;
import ru.krizhanovskiy.p2ptransfers.models.transaction.TransactionService;
import ru.krizhanovskiy.p2ptransfers.models.user.User;
import ru.krizhanovskiy.p2ptransfers.models.user.UserRepository;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
@Tag(name = "Accounts", description = "API для управления счетами пользователей")
public class AccountController {
    private final AccountService accountService;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Создание нового счета", description = "Создаёт новый счет для пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Счет успешно создан"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ErrorResponse",
                                    value = "{\"message\": \"Validation error\"}"
                            )
                    ))
    })
    public ResponseEntity<?> createAccount(Principal principal, @RequestBody @Valid AccountCreateRequest request) {
        User user = userRepository.findByEmail(principal.getName());
        Account createdAccount = accountService.createAccount(user.getId(), request);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/accounts/number/" + createdAccount.getAccountNumber()));
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Получение счета по номеру счета", description = "Возвращает информацию о своём счёте по его номеру")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Счет успешно найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class)
                    )),
            @ApiResponse(responseCode = "404", description = "Счет не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    public ResponseEntity<AccountResponse> getAccountByAccountNumber(Principal principal, @PathVariable String accountNumber) {
        User user = userRepository.findByEmail(principal.getName());
        Account account = accountService.findByAccountNumberAndUserId(accountNumber, user.getId());
        long balance = transactionService.getAccountBalance(account.getId());
        return ResponseEntity.ok(new AccountResponse(account, balance));
    }

    @GetMapping
    @Operation(summary = "Получение списка всех счетов пользователя", description = "Возвращает список всех счетов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список счетов успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class, type = "array"),
                            examples = {
                                    @ExampleObject(
                                            name = "NonEmptyList",
                                            value = "[{\"id\": 1, \"name\": \"Active account\", \"accountNumber\": \"20250707000000000001\", \"status\": \"ACTIVE\", \"createdAt\": \"2025-07-07T19:10:00\", \"balance\": 10000}," +
                                                    "{\"id\": 2, \"name\": \"Closed account\", \"accountNumber\": \"20250707000000000002\", \"status\": \"CLOSED\", \"createdAt\": \"2025-07-06T19:10:00\", \"balance\": 0}]",
                                            summary = "Пример списка с двумя счетами"
                                    ),
                                    @ExampleObject(
                                            name = "EmptyList",
                                            value = "[]",
                                            summary = "Пустой список счетов"
                                    ),
                            }
                    ))
    })
    public ResponseEntity<List<AccountResponse>> getAllAccounts(Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        List<AccountResponse> accounts = accountService.findByUserIdReturnAccountResponse(user.getId());
        return ResponseEntity.ok(accounts);
    }
    @GetMapping("/active")
    @Operation(summary = "Получение списка всех активных счетов пользователя", description = "Возвращает список всех активных счетов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список счетов успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class, type = "array"),
                            examples = {
                                    @ExampleObject(
                                            name = "NonEmptyList",
                                            value = "[{\"id\": 1, \"name\": \"Active account\", \"accountNumber\": \"20250707000000000001\", \"status\": \"ACTIVE\", \"createdAt\": \"2025-07-07T19:10:00\", \"balance\": 10000}," +
                                                    "{\"id\": 2, \"name\": \"Closed account\", \"accountNumber\": \"20250707000000000002\", \"status\": \"ACTIVE\", \"createdAt\": \"2025-07-06T19:10:00\", \"balance\": 0}]",
                                            summary = "Пример списка с двумя счетами"
                                    ),
                                    @ExampleObject(
                                            name = "EmptyList",
                                            value = "[]",
                                            summary = "Пустой список счетов"
                                    ),
                            }
                    ))
    })
    public ResponseEntity<List<AccountResponse>> getAllActiveAccounts(Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        List<AccountResponse> accounts = accountService.findByUserIdAndActiveReturnAccountResponse(user.getId());
        return ResponseEntity.ok(accounts);
    }

    @PatchMapping("/close/{accountNumber}")
    @Operation(summary = "Закрытие счета", description = "Закрывает счет по accountNumber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Счет успешно удалён"),
            @ApiResponse(responseCode = "404", description = "Счет не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    public ResponseEntity<Void> closeAccount(Principal principal, @PathVariable String accountNumber) {
        User user = userRepository.findByEmail(principal.getName());
        accountService.closeAccount(user.getId(), accountNumber);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/number/{accountNumber}/fullname")
    @Operation(summary = "Получение ФИО", description = "Выдаёт ФИО владельца счёта по accountNumber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ФИО успешно выдан",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FullNameResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "FullUserName",
                                            value = "{\"fi0\": \"Иван Иванович И.\"}",
                                            summary = "Пример полного имени пользователя"
                                    ),
                                    @ExampleObject(
                                            name = "UserWithoutMiddleName",
                                            value = "{\"fi0\": \"Иван И.\"}",
                                            summary = "Пример пользователя без отчества"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "404", description = "Счет не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    public ResponseEntity<FullNameResponse> getFullNameByAccountNumber(@PathVariable String accountNumber) {
        String fullName = accountService.getFullNameByAccountNumber(accountNumber);
        return ResponseEntity.ok(new FullNameResponse(fullName));
    }
    record FullNameResponse(String fio){};
}
