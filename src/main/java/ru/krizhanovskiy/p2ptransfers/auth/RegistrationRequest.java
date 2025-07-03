package ru.krizhanovskiy.p2ptransfers.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на регистрацию",
        example = "{\"email\": \"user@example.com\", \"password\": \"password123\", \"firstName\": \"Иван\", \"lastName\": \"Иванов\", \"middleName\": \"Иванович\"}")
public record RegistrationRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(min = 1, max = 100, message = "Email must not exceed 100 characters")
        @Schema(description = "Email пользователя", example = "user@example.com")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        @Schema(description = "Пароль пользователя", example = "password123")
        String password,

        @NotBlank(message = "First name is required")
        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
        @Schema(description = "Имя пользователя", example = "Иван")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        @Schema(description = "Фамилия пользователя", example = "Иванов")
        String lastName,

        @Size(max = 50, message = "Middle name must not exceed 50 characters")
        @Schema(description = "Отчество пользователя (опционально)", example = "Иванович")
        String middleName
) {}