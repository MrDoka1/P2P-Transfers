package ru.krizhanovskiy.p2ptransfers.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import ru.krizhanovskiy.p2ptransfers.exceptions.ErrorResponse;
import ru.krizhanovskiy.p2ptransfers.exceptions.UserAlreadyExistsException;
import ru.krizhanovskiy.p2ptransfers.models.user.User;
import ru.krizhanovskiy.p2ptransfers.models.user.UserService;

import java.net.URI;
import java.security.Principal;

@RestController()
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API для авторизации и регистрации пользователей")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Авторизация пользователя", description = "Аутентифицирует пользователя по email и паролю, возвращает JWT-токен")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная авторизация, возвращён JWT-токен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class),
                            examples = @ExampleObject(
                                    name = "SuccessResponse",
                                    value = "{\"jwt\": \"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ5b3VyQGVtYWlsLmNvbSIsImlhdCI6MTc1MTU3ODUwNywiZXhwIjoxNzUxNjY0OTA3fQ.e-IYFQZjkXvuRvatANmUztccxSCNFRLW0k1zVKr5fUE\"}"
                            )
                    )),
            @ApiResponse(responseCode = "401", description = "Неверный email или пароль",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ErrorResponse",
                                    value = "{\"message\": \"Invalid email or password\"}"
                            )
                    ))
    })
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя", description = "Создаёт нового пользователя и перенаправляет на эндпоинт логина")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан, редирект на /login",
                    headers = @Header(name = "Location", description = "URL для редиректа", schema = @Schema(type = "string", example = "/login")),
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "RedirectResponse",
                                    value = "{}"
                            )
                    )),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ErrorResponse",
                                    value = "{\"message\": \"User with this email already exists\"}"
                            )
                    ))
    })
    public ResponseEntity<?> register(@RequestBody @Valid RegistrationRequest request) {
        authService.registerUser(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/login"));
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @GetMapping("/refresh")
    @Operation(summary = "Обновление JWT", description = "Возвращает новый JWT-токен")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно возвращён JWT-токен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class),
                            examples = @ExampleObject(
                                    name = "SuccessResponse",
                                    value = "{\"jwt\": \"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ5b3VyQGVtYWlsLmNvbSIsImlhdCI6MTc1MTU3ODUwNywiZXhwIjoxNzUxNjY0OTA3fQ.e-IYFQZjkXvuRvatANmUztccxSCNFRLW0k1zVKr5fUE\"}"
                            )
                    )
            )
    })
    public ResponseEntity<?> refresh(Principal principal) {
        User user = userService.findByEmail(principal.getName());
        return ResponseEntity.ok(new AuthenticationResponse(jwtService.generateToken(user)));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserIsExistException(UserAlreadyExistsException ignored) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("User with this email already exists"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ignored) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Invalid email or password"));
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ignored) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Invalid email or password"));
    }
}