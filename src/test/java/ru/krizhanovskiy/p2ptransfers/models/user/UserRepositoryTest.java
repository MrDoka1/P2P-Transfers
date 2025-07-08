package ru.krizhanovskiy.p2ptransfers.models.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import ru.krizhanovskiy.p2ptransfers.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private UserRepository userRepository;

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
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void save_success() {
        // Arrange
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("hash")
                .firstName("John")
                .lastName("Doe")
                .middleName("A")
                .build();

        // Мокаем jdbcTemplate.update(...) и вручную кладём ключ
        when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder keyHolder = invocation.getArgument(1);
                    keyHolder.getKeyList().add(Map.of("id", 1L));
                    return 1;
                });

        // Act
        User result = userRepository.save(user);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(user.getEmail(), result.getEmail());
    }


    @Test
    void findByEmail_success() {
        when(jdbcTemplate.queryForObject(eq("SELECT * FROM users WHERE email = ?"), any(RowMapper.class), eq("test@example.com")))
                .thenReturn(user);

        User result = userRepository.findByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(jdbcTemplate).queryForObject(eq("SELECT * FROM users WHERE email = ?"), any(RowMapper.class), eq("test@example.com"));
    }

    @Test
    void findByEmail_notFound_throwsException() {
        when(jdbcTemplate.queryForObject(eq("SELECT * FROM users WHERE email = ?"), any(RowMapper.class), eq("test@example.com")))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(UserNotFoundException.class, () -> userRepository.findByEmail("test@example.com"));
        verify(jdbcTemplate).queryForObject(eq("SELECT * FROM users WHERE email = ?"), any(RowMapper.class), eq("test@example.com"));
    }

    @Test
    void findById_success() {
        when(jdbcTemplate.queryForObject(eq("SELECT * FROM users WHERE id = ?"), any(RowMapper.class), eq(1L)))
                .thenReturn(user);

        User result = userRepository.findById(1L);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        verify(jdbcTemplate).queryForObject(eq("SELECT * FROM users WHERE id = ?"), any(RowMapper.class), eq(1L));
    }

    @Test
    void findById_notFound_throwsException() {
        when(jdbcTemplate.queryForObject(eq("SELECT * FROM users WHERE id = ?"), any(RowMapper.class), eq(1L)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(UserNotFoundException.class, () -> userRepository.findById(1L));
        verify(jdbcTemplate).queryForObject(eq("SELECT * FROM users WHERE id = ?"), any(RowMapper.class), eq(1L));
    }

    @Test
    void findAll_success() {
        when(jdbcTemplate.query(eq("SELECT * FROM users"), any(RowMapper.class))).thenReturn(List.of(user));

        List<User> result = userRepository.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getEmail(), result.get(0).getEmail());
        verify(jdbcTemplate).query(eq("SELECT * FROM users"), any(RowMapper.class));
    }

    @Test
    void delete_success() {
        when(jdbcTemplate.update(eq("DELETE FROM users WHERE id = ?"), eq(1L))).thenReturn(1);

        userRepository.delete(1L);

        verify(jdbcTemplate).update(eq("DELETE FROM users WHERE id = ?"), eq(1L));
    }
}