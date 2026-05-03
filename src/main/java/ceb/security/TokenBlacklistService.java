package ceb.security;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;

/**
 * Service quản lý blacklist token
 * Khi user logout, token sẽ được thêm vào blacklist
 * Mọi request sử dụng token blacklist sẽ bị từ chối
 */
@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Thêm token vào blacklist
     * @param token JWT token cần blacklist
     * @param expirationTime Thời gian hết hạn của token (Unix timestamp)
     */
    public void blacklistToken(String token, long expirationTime) {
        blacklistedTokens.put(token, expirationTime);
    }

    /**
     * Kiểm tra token có bị blacklist hay không
     * @param token JWT token cần kiểm tra
     * @return true nếu token đã bị blacklist, false nếu chưa
     */
    public boolean isTokenBlacklisted(String token) {
        if (blacklistedTokens.containsKey(token)) {
            Long expirationTime = blacklistedTokens.get(token);
            long currentTime = System.currentTimeMillis();

            // Nếu token đã hết hạn, xóa khỏi blacklist (optional optimization)
            if (currentTime > expirationTime) {
                blacklistedTokens.remove(token);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Lấy kích thước hiện tại của blacklist (cho monitoring/debugging)
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }

    /**
     * Clear toàn bộ blacklist (sử dụng cho testing hoặc admin operations)
     */
    public void clearBlacklist() {
        blacklistedTokens.clear();
    }
}

