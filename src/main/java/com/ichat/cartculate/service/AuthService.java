package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.LoginRequest;
import com.ichat.cartculate.dto.SignupRequest;
import com.ichat.cartculate.dto.UserDto;
import com.ichat.cartculate.entity.User;
import com.ichat.cartculate.entity.UserMode;
import com.ichat.cartculate.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Backs the username/password login screen. Deliberately NOT using full
 * Spring Security (see the pom.xml comment on spring-security-crypto) -
 * this is just credential verification, no session/JWT/filter chain. The
 * frontend persists the returned user id locally (AsyncStorage, see
 * session.ts) and sends it as the existing {userId} path variable on every
 * other endpoint, exactly like the hardcoded CURRENT_USER_ID did before.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto login(LoginRequest request) {
        if (isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect username or password"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect username or password");
        }

        return toDto(user);
    }

    public UserDto signup(SignupRequest request) {
        if (isBlank(request.getName()) || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name, username, and password are required");
        }
        if (request.getPassword().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already taken");
        }

        User user = new User();
        user.setName(request.getName());
        // No email field on the signup screen (see SignupRequest's javadoc) - User.email is
        // still NOT NULL/unique at the DB level, so this placeholder satisfies that constraint
        // without ever surfacing it in the UI.
        user.setEmail(request.getUsername() + "@cartculate.local");
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCurrentMode(UserMode.HOME);

        return toDto(userRepository.save(user));
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getUsername(),
                user.getCurrentMode().name()
        );
    }
}
