package ru.krizhanovskiy.p2ptransfers.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.krizhanovskiy.p2ptransfers.expressions.UserAlreadyExistsException;
import ru.krizhanovskiy.p2ptransfers.expressions.UserNotFoundException;
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

    public void registerUser(RegistrationRequest request) {
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

        userService.createUser(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        final String jwt = jwtService.generateToken(userDetails);
        return new AuthenticationResponse(jwt);
    }

}