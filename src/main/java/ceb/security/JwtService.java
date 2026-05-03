package ceb.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ceb.domain.entity.Users;
import ceb.exception.InvalidJwtTokenException;
import ceb.exception.JwtTokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String PASSWORD_RESET_TOKEN_TYPE = "PASSWORD_RESET";

    private final SecretKey signingKey;
    private final long expirationMs;
    private final long resetPasswordExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.reset-password-expiration-ms:900000}") long resetPasswordExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.resetPasswordExpirationMs = resetPasswordExpirationMs;
    }

    public String generateToken(Users user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getUserId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public long getResetPasswordExpirationMs() {
        return resetPasswordExpirationMs;
    }

    public String generatePasswordResetToken(Users user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getUserId())
                .claim("type", PASSWORD_RESET_TOKEN_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(resetPasswordExpirationMs)))
                .signWith(signingKey)
                .compact();
    }

    public long getTokenExpiration(String token) {
        Claims claims = parseClaims(token);
        Date expirationDate = claims.getExpiration();
        return expirationDate != null ? expirationDate.getTime() : 0;
    }

    public JwtUserClaims verifyAndExtract(String token) {
        Claims claims = parseClaims(token);
        Number userIdValue = claims.get("userId", Number.class);
        String username = claims.getSubject();

        if (userIdValue == null || username == null || username.isBlank()) {
            throw new InvalidJwtTokenException();
        }

        return new JwtUserClaims(username, userIdValue.intValue());
    }

    public JwtUserClaims verifyPasswordResetToken(String token) {
        Claims claims = parseClaims(token);
        String tokenType = claims.get("type", String.class);
        Number userIdValue = claims.get("userId", Number.class);
        String username = claims.getSubject();

        if (!PASSWORD_RESET_TOKEN_TYPE.equals(tokenType)
                || userIdValue == null
                || username == null
                || username.isBlank()) {
            throw new InvalidJwtTokenException();
        }

        return new JwtUserClaims(username, userIdValue.intValue());
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new JwtTokenExpiredException();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidJwtTokenException();
        }
    }

    public record JwtUserClaims(String username, int userId) {
    }
}
