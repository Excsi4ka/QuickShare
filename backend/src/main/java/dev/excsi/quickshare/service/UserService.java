package dev.excsi.quickshare.service;

import dev.excsi.quickshare.exception.UserNotFoundException;
import dev.excsi.quickshare.model.UserEntity;
import dev.excsi.quickshare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity getUserByUUID(UUID uuid) {
        return userRepository.findById(uuid)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found, Invalid id: %s", uuid)));
    }

    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found, Invalid email: %s", email)));
    }

}
