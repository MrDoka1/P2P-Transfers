package ru.krizhanovskiy.p2ptransfers.models.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.krizhanovskiy.p2ptransfers.annotations.Timed;
import ru.krizhanovskiy.p2ptransfers.exceptions.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Timed
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username);
    }

    @Timed
    public User findByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email);
    }

    @Timed
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}