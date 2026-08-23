package dev.excsi.quickshare.controller;

import dev.excsi.quickshare.dto.EmailPasswordDto;
import dev.excsi.quickshare.dto.UserDto;
import dev.excsi.quickshare.model.UserEntity;
import dev.excsi.quickshare.service.UserService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth/")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public void login(@RequestBody EmailPasswordDto emailAndPassword) {

    }

    @GetMapping("/me")
    public UserDto me(JwtAuthenticationToken authenticationToken) {
        UserEntity user = userService.getUserByUUID(UUID.fromString(authenticationToken.getName()));
        return new UserDto(user.getId(), user.getDisplayName(), user.getEmail());
    }

}
