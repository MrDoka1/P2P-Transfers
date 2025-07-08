package ru.krizhanovskiy.p2ptransfers.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на аутентификацию",
        example = "{\"email\": \"user@example.com\", \"password\": \"password123\"}")
public record AuthenticationRequest(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email format is invalid")
        @Schema(description = "Email пользователя", example = "user@example.com")
        String email,

        @NotBlank(message = "Password cannot be blank")
        @Schema(description = "Пароль пользователя", example = "password123")
        String password
) {}