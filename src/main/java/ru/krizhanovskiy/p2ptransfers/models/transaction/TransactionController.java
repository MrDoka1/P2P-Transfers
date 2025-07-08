package ru.krizhanovskiy.p2ptransfers.models.transaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import ru.krizhanovskiy.p2ptransfers.models.account.AccountService;
import ru.krizhanovskiy.p2ptransfers.models.user.User;
import ru.krizhanovskiy.p2ptransfers.models.user.UserRepository;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "API для управления транзакциями")
public class TransactionController {
    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Создание транзакции", description = "Создаёт новую транзакцию между счетами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Транзакция успешно создана"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "404", description = "Счёт не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    public ResponseEntity<Transaction> createTransaction(@RequestBody @Valid TransactionCreateRequest request) {
        Transaction transaction = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Подтверждение транзакции", description = "Подтверждает транзакцию и списывает/зачисляет средства")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Транзакция успешно подтверждена"),
            @ApiResponse(responseCode = "400", description = "Недостаточно средств или неверное состояние",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    public ResponseEntity<?> confirmTransaction(@PathVariable Long id) {
        transactionService.confirmTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Отмена транзакции", description = "Отменяет транзакцию, если она в состоянии PENDING")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Транзакция успешно отменена"),
            @ApiResponse(responseCode = "400", description = "Неверное состояние транзакции",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    public ResponseEntity<?> cancelTransaction(Principal principal, @PathVariable Long id) {
        User user = userRepository.findByEmail(principal.getName());
        transactionService.cancelTransaction(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/balance/{accountId}")
    @Operation(summary = "Получение баланса счёта", description = "Возвращает текущий баланс счёта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Баланс успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "integer", example = "15000")
                    ))
    })
    public ResponseEntity<Long> getAccountBalance(Principal principal, @PathVariable Long accountId) {
        User user = userRepository.findByEmail(principal.getName());
        accountService.findByIdAndUserId(accountId, user.getId());
        Long balance = transactionService.getAccountBalance(accountId);
        return ResponseEntity.ok(balance);
    }
}