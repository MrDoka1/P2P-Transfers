package ru.krizhanovskiy.p2ptransfers.auth;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.krizhanovskiy.p2ptransfers.annotations.Timed;
import ru.krizhanovskiy.p2ptransfers.exceptions.UserAlreadyExistsException;
import ru.krizhanovskiy.p2ptransfers.exceptions.UserNotFoundException;
import ru.krizhanovskiy.p2ptransfers.models.user.User;
import ru.krizhanovskiy.p2ptransfers.models.user.UserService;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Timed
    public User registerUser(RegistrationRequest request) {
        try {
            userService.findByEmail(request.email());
            throw new UserAlreadyExistsException();
        } catch (UserNotFoundException ignore) {}


        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .middleName(request.middleName())
                .build();

        User savedUser = userService.saveUser(user);
        log.info("Пользователь с email '{}' успешно зарегистрирован с id={}", savedUser.getEmail(), savedUser.getId());
        return savedUser;
    }

    @Timed
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        final String jwt = jwtService.generateToken(userDetails);
        log.info("Пользователь с email '{}' успешно аутентифицирован", request.email());
        return new AuthenticationResponse(jwt);
    }

}