package ru.krizhanovskiy.p2ptransfers.models.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.krizhanovskiy.p2ptransfers.exceptions.UserNotFoundException;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User user;
    private Principal principal;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
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
    void getUser_success() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);

        ResponseEntity<UserResponse> response = userController.getUser(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserResponse result = response.getBody();
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getLastName(), result.getLastName());
        assertEquals(user.getMiddleName(), result.getMiddleName());
        verify(userService).findByEmail("test@example.com");
    }

    @Test
    void getUser_userNotFound_throwsException() {
        when(userService.findByEmail("test@example.com")).thenThrow(new UserNotFoundException());

        assertThrows(UserNotFoundException.class, () -> userController.getUser(principal));
        verify(userService).findByEmail("test@example.com");
    }
}