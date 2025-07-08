package ru.krizhanovskiy.p2ptransfers.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import ru.krizhanovskiy.p2ptransfers.exceptions.ErrorResponse;
import ru.krizhanovskiy.p2ptransfers.exceptions.UserAlreadyExistsException;
import ru.krizhanovskiy.p2ptransfers.models.user.User;
import ru.krizhanovskiy.p2ptransfers.models.user.UserService;

import java.net.URI;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private RegistrationRequest registrationRequest;
    private AuthenticationRequest authenticationRequest;
    private User user;
    private Principal principal;

    @BeforeEach
    void setUp() {
        registrationRequest = new RegistrationRequest(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                "Middle"
        );

        authenticationRequest = new AuthenticationRequest(
                "test@example.com",
                "password123"
        );

        user = User.builder()
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .middleName("Middle")
                .build();

        principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test@example.com");
    }

    @Test
    void login_success() {
        AuthenticationResponse authResponse = new AuthenticationResponse("jwtToken");
        when(authService.authenticate(authenticationRequest)).thenReturn(authResponse);

        ResponseEntity<AuthenticationResponse> response = authController.login(authenticationRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());
        verify(authService).authenticate(authenticationRequest);
    }

    @Test
    void register_success() {
        when(authService.registerUser(registrationRequest)).thenReturn(user);

        ResponseEntity<?> response = authController.register(registrationRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(URI.create("/login"), response.getHeaders().getLocation());
        verify(authService).registerUser(registrationRequest);
    }

    @Test
    void refresh_success() {
        when(userService.findByEmail(principal.getName())).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("newJwtToken");

        ResponseEntity<?> response = authController.refresh(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("newJwtToken", ((AuthenticationResponse) response.getBody()).jwt());
        verify(userService).findByEmail(principal.getName());
        verify(jwtService).generateToken(user);
    }

    @Test
    void handleUserAlreadyExistsException() {
        ResponseEntity<ErrorResponse> response = authController.handleUserIsExistException(new UserAlreadyExistsException());

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("User with this email already exists", response.getBody().error());
    }

    @Test
    void handleBadCredentialsException() {
        ResponseEntity<ErrorResponse> response = authController.handleBadCredentialsException(new BadCredentialsException(""));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody().error());
    }
}