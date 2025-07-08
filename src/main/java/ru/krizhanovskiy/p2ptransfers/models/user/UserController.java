package ru.krizhanovskiy.p2ptransfers.models.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API для управления пользователями")
public class UserController {
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Получение информации о пользователе", description = "Получение информации о себе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "FullUser",
                                            value = "{\"id\": 1, \"email\": \"ivan@example.com\", \"firstName\": \"Иван\", \"lastName\": \"Иванов\", \"middleName\": \"Иванович\"}",
                                            summary = "Пример полного профиля пользователя"
                                    ),
                                    @ExampleObject(
                                            name = "UserWithNullMiddleName",
                                            value = "{\"id\": 2, \"email\": \"ivan@example.com\", \"firstName\": \"Иван\", \"lastName\": \"Иванов\", \"middleName\": null}",
                                            summary = "Пример пользователя без middleName"
                                    )
                            }
                    ))
    })
    public ResponseEntity<UserResponse> getUser(Principal principal) {
        User user = userService.findByEmail(principal.getName());
        return ResponseEntity.ok(new UserResponse(user));
    }
}