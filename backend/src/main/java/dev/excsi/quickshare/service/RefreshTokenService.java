package dev.excsi.quickshare.service;

import dev.excsi.quickshare.model.RefreshTokenEntity;
import dev.excsi.quickshare.model.UserEntity;
import dev.excsi.quickshare.repository.RefreshTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "jwt_refresh_token";

    public static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshTokenEntity createToken(UserEntity user) {
        Instant now = Instant.now(Clock.systemUTC());
        RefreshTokenEntity refreshToken = new RefreshTokenEntity(
                UUID.randomUUID(),
                user,
                now,
                now.plus(REFRESH_TOKEN_TTL)
        );

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshTokenEntity rotateToken(String rawRefreshToken) {
        UUID refreshTokenId = parseRefreshToken(rawRefreshToken);
        RefreshTokenEntity currentToken = refreshTokenRepository.findById(refreshTokenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (currentToken.getExpiresAt().isBefore(Instant.now(Clock.systemUTC()))) {
            refreshTokenRepository.delete(currentToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        UserEntity user = currentToken.getUser();
        refreshTokenRepository.delete(currentToken);
        return createToken(user);
    }

    private UUID parseRefreshToken(String rawRefreshToken) {
        try {
            return UUID.fromString(rawRefreshToken);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
