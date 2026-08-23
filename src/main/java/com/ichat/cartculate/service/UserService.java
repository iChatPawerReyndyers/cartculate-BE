package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.UserDto;
import com.ichat.cartculate.entity.User;
import com.ichat.cartculate.entity.UserMode;
import com.ichat.cartculate.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return toDto(user);
    }

    /** Persists the Home/Away mode toggle from the Cart screen. */
    public UserDto updateMode(Long userId, String mode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        UserMode parsedMode;
        try {
            parsedMode = UserMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid mode: " + mode + " (expected HOME or AWAY)");
        }

        user.setCurrentMode(parsedMode);
        return toDto(userRepository.save(user));
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