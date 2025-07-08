package ru.krizhanovskiy.p2ptransfers.models.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import ru.krizhanovskiy.p2ptransfers.exceptions.UserNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

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
    }

    @Test
    void loadUserByUsername_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        UserDetails result = userService.loadUserByUsername("test@example.com");

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getUsername());
        assertEquals(user.getPasswordHash(), result.getPassword());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_userNotFound_throwsException() {
        when(userRepository.findByEmail("test@example.com")).thenThrow(new UserNotFoundException());

        assertThrows(UserNotFoundException.class, () -> userService.loadUserByUsername("test@example.com"));
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void findByEmail_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        User result = userService.findByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void findByEmail_userNotFound_throwsException() {
        when(userRepository.findByEmail("test@example.com")).thenThrow(new UserNotFoundException());

        assertThrows(UserNotFoundException.class, () -> userService.findByEmail("test@example.com"));
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void saveUser_success() {
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.saveUser(user);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository).save(user);
    }
}