package dev.excsi.quickshare.service;

import dev.excsi.quickshare.exception.BadInputException;
import dev.excsi.quickshare.exception.UserAlreadyRegisteredException;
import dev.excsi.quickshare.exception.UserNotFoundException;
import dev.excsi.quickshare.model.UserEntity;
import dev.excsi.quickshare.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final Pattern passwordRegex = Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$");

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity getUserByUUID(UUID uuid) {
        return userRepository.findById(uuid)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found, Invalid id: %s", uuid)));
    }

    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User not found, Invalid email: %s", email)));
        // UsernameNotFoundException is needed for the DaoAuthProvider to recognize it as an auth failure
    }

    public UserEntity registerWithUsernamePassword(String displayName, String email, String password) {
        if (displayName == null || displayName.isBlank())
            throw new BadInputException("Display name cannot blank");
        if (email == null || email.isBlank())
            throw new BadInputException("Email cannot blank");
        if (password == null || password.isBlank())
            throw new BadInputException("Password cannot blank");
        if (!passwordRegex.matcher(password).matches())
            throw new BadInputException("Password does not match character/length requirements");
        if (userRepository.existsByEmail(email))
            throw new UserAlreadyRegisteredException(email);
        UserEntity user = new UserEntity(displayName, email, passwordEncoder.encode(password));
        userRepository.save(user);
        return user;
    }
}
