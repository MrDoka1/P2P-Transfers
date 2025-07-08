package ru.krizhanovskiy.p2ptransfers.models.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TransactionCreateRequest(
        @NotBlank(message = "The account source number cannot be empty")
        @Pattern(regexp = "^\\d{20}$", message = "The account number must contain exactly 20 digits")
        @Schema(description = "Номер счёта-источника", example = "40817810322062033995")
        String sourceAccountNumber,

        @NotBlank(message = "The recipient's account number cannot be empty")
        @Pattern(regexp = "^\\d{20}$", message = "The account number must contain exactly 20 digits")
        @Schema(description = "Номер счёта-получателя", example = "40817810322062033996")
        String recipientAccountNumber,

        @Min(value = 1, message = "The amount must be positive")
        @Schema(description = "Сумма транзакции в копейках", example = "5000")
        long amount
) {}