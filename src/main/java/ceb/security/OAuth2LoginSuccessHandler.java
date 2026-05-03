package ceb.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import ceb.domain.entity.Users;
import ceb.service.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtService jwtService;
    private final JwtCookieService jwtCookieService;
    private final String frontendRedirectUrl;
    private final boolean useCookieRedirect;

    public OAuth2LoginSuccessHandler(
            AuthService authService,
            JwtService jwtService,
            JwtCookieService jwtCookieService,
            @Value("${app.auth.oauth2.redirect-url:http://localhost:3000}") String frontendRedirectUrl,
            @Value("${app.auth.oauth2.use-cookie-redirect:false}") boolean useCookieRedirect) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.jwtCookieService = jwtCookieService;
        this.frontendRedirectUrl = frontendRedirectUrl;
        this.useCookieRedirect = useCookieRedirect;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String fullName = oauth2User.getAttribute("name");

        Users user = authService.loginWithGoogle(email, fullName);
        String token = jwtService.generateToken(user);

        if (useCookieRedirect) {
            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookieService.createAccessTokenCookie(token));
            response.sendRedirect(frontendRedirectUrl);
            return;
        }

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("token", URLEncoder.encode(token, StandardCharsets.UTF_8))
                .build(true)
                .toUriString();
        response.sendRedirect(redirectUrl);
    }
}
