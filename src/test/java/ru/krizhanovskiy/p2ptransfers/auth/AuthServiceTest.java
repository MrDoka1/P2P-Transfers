package ru.krizhanovskiy.p2ptransfers.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.krizhanovskiy.p2ptransfers.exceptions.UserAlreadyExistsException;
import ru.krizhanovskiy.p2ptransfers.exceptions.UserNotFoundException;
import ru.krizhanovskiy.p2ptransfers.models.user.User;
import ru.krizhanovskiy.p2ptransfers.models.user.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private RegistrationRequest registrationRequest;
    private AuthenticationRequest authenticationRequest;
    private User user;
    private UserDetails userDetails;

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

        userDetails = user;
    }

    @Test
    void registerUser_success() {
        when(userService.findByEmail(registrationRequest.email())).thenThrow(new UserNotFoundException());
        when(passwordEncoder.encode(registrationRequest.password())).thenReturn("encodedPassword");
        when(userService.saveUser(any(User.class))).thenReturn(user);

        User result = authService.registerUser(registrationRequest);

        assertNotNull(result);
        assertEquals(registrationRequest.email(), result.getEmail());
        assertEquals(registrationRequest.firstName(), result.getFirstName());
        assertEquals(registrationRequest.lastName(), result.getLastName());
        assertEquals(registrationRequest.middleName(), result.getMiddleName());
        verify(userService).saveUser(any(User.class));
        verify(passwordEncoder).encode(registrationRequest.password());
    }

    @Test
    void registerUser_userAlreadyExists_throwsException() {
        when(userService.findByEmail(registrationRequest.email())).thenReturn(user);

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(registrationRequest));
        verify(userService, never()).saveUser(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void authenticate_success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        when(userDetailsService.loadUserByUsername(authenticationRequest.email())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwtToken");

        AuthenticationResponse response = authService.authenticate(authenticationRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.jwt());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername(authenticationRequest.email());
        verify(jwtService).generateToken(userDetails);
    }
}