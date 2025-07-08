package ru.krizhanovskiy.p2ptransfers.models.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountCreateRequest(
        @NotBlank(message = "Name cannot be blank")
        @Size(max = 40, message = "Name must not exceed 40 characters")
        @Schema(description = "Название счёта", example = "Основной сберегательный счёт")
        String name,

        @Min(value = 0, message = "Balance must be non-negative")
        @Schema(description = "Начальный баланс счёта в копейках", example = "10000")
        long balance
) {}