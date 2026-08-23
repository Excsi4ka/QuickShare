package dev.excsi.quickshare.controller;

import dev.excsi.quickshare.dto.EmailPasswordDto;
import dev.excsi.quickshare.dto.JwtTokenResponse;
import dev.excsi.quickshare.dto.RegisterUserDto;
import dev.excsi.quickshare.dto.UserDto;
import dev.excsi.quickshare.model.UserEntity;
import dev.excsi.quickshare.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/")
public class AuthController {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final JwtEncoder jwtEncoder;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtEncoder jwtEncoder) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterUserDto registerUser) {

    }

    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponse> login(@RequestBody EmailPasswordDto emailAndPassword) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(emailAndPassword.email(), emailAndPassword.password())
            );
            UserEntity user = (UserEntity) authentication.getPrincipal();

            Instant now = Instant.now();
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("self")
                    .issuedAt(now)
                    .expiresAt(now.plus(1, ChronoUnit.HOURS))
                    .subject(user.getId().toString()) // UUID
                    .build();

            String jwtToken = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new JwtTokenResponse(jwtToken));
        } catch (AuthenticationException authException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/me")
    public UserDto me(JwtAuthenticationToken authenticationToken) {
        UserEntity user = userService.getUserByUUID(UUID.fromString(authenticationToken.getName()));
        return new UserDto(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
