package dev.excsi.quickshare.controller;

import dev.excsi.quickshare.dto.EmailPasswordDto;
import dev.excsi.quickshare.dto.JwtTokenResponse;
import dev.excsi.quickshare.dto.RegisterUserDto;
import dev.excsi.quickshare.dto.UserDto;
import dev.excsi.quickshare.model.RefreshTokenEntity;
import dev.excsi.quickshare.model.UserEntity;
import dev.excsi.quickshare.service.JwtTokenService;
import dev.excsi.quickshare.service.RefreshTokenService;
import dev.excsi.quickshare.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth/")
public class AuthController {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenService jwtTokenService;

    private final RefreshTokenService refreshTokenService;

    public AuthController(
            UserService userService,
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody RegisterUserDto registerUser) {
        UserEntity user = userService.registerWithUsernamePassword(
                registerUser.username(),
                registerUser.email(),
                registerUser.password());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserDto(user.getId(), user.getEmail(), user.getDisplayName()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtTokenResponse> refresh(@CookieValue(value = RefreshTokenService.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        RefreshTokenEntity rotatedRefreshToken = refreshTokenService.rotateToken(refreshToken);
        String jwtToken = jwtTokenService.createAccessToken(rotatedRefreshToken.getUser());

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie(rotatedRefreshToken).toString())
                .body(new JwtTokenResponse(jwtToken));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponse> login(@RequestBody EmailPasswordDto emailAndPassword) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(emailAndPassword.email(), emailAndPassword.password())
            );
            UserEntity user = (UserEntity) authentication.getPrincipal();

            String jwtToken = jwtTokenService.createAccessToken(user);
            RefreshTokenEntity refreshToken = refreshTokenService.createToken(user);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie(refreshToken).toString())
                    .body(new JwtTokenResponse(jwtToken));
        } catch (AuthenticationException authException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/me")
    public UserDto me(JwtAuthenticationToken authenticationToken) {
        UserEntity user = userService.getUserByUUID(UUID.fromString(authenticationToken.getName()));
        return new UserDto(user.getId(), user.getEmail(), user.getDisplayName());
    }

    private ResponseCookie refreshTokenCookie(RefreshTokenEntity refreshToken) {
        return ResponseCookie.from(RefreshTokenService.REFRESH_TOKEN_COOKIE_NAME, refreshToken.getId().toString())
                .httpOnly(true)
                .path("/api/auth/refresh")
                .maxAge(RefreshTokenService.REFRESH_TOKEN_TTL)
                .sameSite("Lax")
                .build();
    }
}
