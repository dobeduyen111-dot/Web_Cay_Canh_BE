package ceb.security;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * JWT Cookie Service - Xử lý tạo, xóa và đọc JWT cookie với cấu hình bảo mật.
 * 
 * Cookie attributes bảo mật:
 * - HttpOnly: true - Ngăn JavaScript truy cập, chỉ gửi qua HTTP(S)
 * - Secure: true (prod), false (dev) - Chỉ gửi qua HTTPS
 * - SameSite: Strict/Lax - CSRF protection, chỉ gửi cùng site
 * - Path: /api - Scope tối thiểu, chỉ gửi khi request /api/**
 * - Domain: localhost/example.com - Scope tối thiểu, chỉ gửi cho domain cụ thể
 */
@Component
public class JwtCookieService {

    private final String cookieName;
    private final String cookiePath;
    private final String cookieDomain;
    private final boolean secure;
    private final String sameSite;
    private final JwtService jwtService;

    public JwtCookieService(
            @Value("${app.jwt.cookie.name:access_token}") String cookieName,
            @Value("${app.jwt.cookie.path:/api}") String cookiePath,
            @Value("${app.jwt.cookie.domain:localhost}") String cookieDomain,
            @Value("${app.jwt.cookie.secure:false}") boolean secure,
            @Value("${app.jwt.cookie.same-site:Strict}") String sameSite,
            JwtService jwtService) {
        this.cookieName = cookieName;
        this.cookiePath = cookiePath;
        this.cookieDomain = cookieDomain;
        this.secure = secure;
        this.sameSite = sameSite;
        this.jwtService = jwtService;
    }

    /**
     * Tạo Access Token Cookie với attributes bảo mật.
     * 
     * HttpOnly: true - JS không đọc được (hiện trong DevTools nhưng JS không thể truy cập)
     * Secure: chỉ gửi qua HTTPS (prod), HTTP được (dev)
     * SameSite: Strict (ngặt nhất) hoặc Lax - CSRF protection
     * Path: /api - chỉ gửi khi request /api/** endpoints
     * Domain: localhost - chỉ gửi cho domain cụ thể
     */
    public String createAccessTokenCookie(String token) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(secure)
                .path(cookiePath)
                .domain(cookieDomain)
                .sameSite(sameSite)
                .maxAge(Duration.ofMillis(jwtService.getExpirationMs()))
                .build()
                .toString();
    }

    public String clearAccessTokenCookie() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secure)
                .path(cookiePath)
                .domain(cookieDomain)
                .sameSite(sameSite)
                .maxAge(Duration.ZERO)
                .build()
                .toString();
    }

    public Optional<String> resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                    return Optional.of(cookie.getValue());
                }
            }
        }

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return Optional.of(authorizationHeader.substring(7));
        }

        return Optional.empty();
    }
}
