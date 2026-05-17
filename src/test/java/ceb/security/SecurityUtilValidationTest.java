package ceb.security;

import java.time.Instant;
import java.util.Map;

import ceb.TestData;
import ceb.domain.entity.Users;
import ceb.exception.ErrorCode;
import ceb.exception.InvalidJwtTokenException;
import ceb.service.service.AuthService;
import ceb.util.VnpayUtils;
import ceb.validation.PasswordValidator;
import ceb.validation.PhoneNumberValidator;
import ceb.validation.PriceValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityUtilValidationTest {

    private static final String JWT_SECRET = "12345678901234567890123456789012";

    @Test
    void jwtServiceGeneratesAndVerifiesAccessAndResetTokens() {
        JwtService jwtService = new JwtService(JWT_SECRET, 60_000, 30_000);
        Users user = TestData.user();

        String accessToken = jwtService.generateToken(user);
        JwtService.JwtUserClaims accessClaims = jwtService.verifyAndExtract(accessToken);

        assertThat(accessClaims.username()).isEqualTo(user.getEmail());
        assertThat(accessClaims.userId()).isEqualTo(user.getUserId());
        assertThat(jwtService.getTokenExpiration(accessToken)).isGreaterThan(Instant.now().toEpochMilli());

        String resetToken = jwtService.generatePasswordResetToken(user);
        assertThat(jwtService.verifyPasswordResetToken(resetToken).username()).isEqualTo(user.getEmail());
        assertThatThrownBy(() -> jwtService.verifyPasswordResetToken(accessToken))
                .isInstanceOf(InvalidJwtTokenException.class);
    }

    @Test
    void jwtCookieServiceCreatesClearsAndResolvesToken() {
        JwtCookieService service = new JwtCookieService(
                "access_token",
                "/api",
                "localhost",
                false,
                "Strict",
                new JwtService(JWT_SECRET, 60_000, 30_000));

        String cookie = service.createAccessTokenCookie("token");
        assertThat(cookie).contains("access_token=token", "HttpOnly", "Path=/api");
        assertThat(service.clearAccessTokenCookie()).contains("Max-Age=0");

        MockHttpServletRequest cookieRequest = new MockHttpServletRequest();
        cookieRequest.setCookies(new Cookie("access_token", "cookie-token"));
        assertThat(service.resolveToken(cookieRequest)).contains("cookie-token");

        MockHttpServletRequest bearerRequest = new MockHttpServletRequest();
        bearerRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer bearer-token");
        assertThat(service.resolveToken(bearerRequest)).contains("bearer-token");
    }

    @Test
    void tokenBlacklistRemovesExpiredTokens() {
        TokenBlacklistService service = new TokenBlacklistService();

        service.blacklistToken("active", System.currentTimeMillis() + 60_000);
        service.blacklistToken("expired", System.currentTimeMillis() - 1);

        assertThat(service.isTokenBlacklisted("active")).isTrue();
        assertThat(service.isTokenBlacklisted("expired")).isFalse();
        assertThat(service.getBlacklistSize()).isEqualTo(1);
        service.clearBlacklist();
        assertThat(service.getBlacklistSize()).isZero();
    }

    @Test
    void oauth2SuccessHandlerRedirectsWithCookieWhenConfigured() throws Exception {
        AuthService authService = mock(AuthService.class);
        JwtService jwtService = mock(JwtService.class);
        JwtCookieService cookieService = mock(JwtCookieService.class);
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn("oauth@example.com");
        when(oauth2User.getAttribute("name")).thenReturn("OAuth User");
        when(authService.loginWithGoogle("oauth@example.com", "OAuth User")).thenReturn(TestData.user());
        when(jwtService.generateToken(TestData.user())).thenReturn("jwt");
        when(jwtService.generateToken(org.mockito.ArgumentMatchers.any(Users.class))).thenReturn("jwt");
        when(cookieService.createAccessTokenCookie("jwt")).thenReturn("access_token=jwt; HttpOnly");

        OAuth2LoginSuccessHandler handler = new OAuth2LoginSuccessHandler(
                authService,
                jwtService,
                cookieService,
                "https://frontend.example.test",
                true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(
                new MockHttpServletRequest(),
                response,
                new TestingAuthenticationToken(oauth2User, null));

        assertThat(response.getRedirectedUrl()).isEqualTo("https://frontend.example.test");
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).contains("access_token=jwt");
    }

    @Test
    void oauth2FailureHandlerRedirectsWithFailureFlag() throws Exception {
        OAuth2LoginFailureHandler handler = new OAuth2LoginFailureHandler("https://frontend.example.test/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(new MockHttpServletRequest(), response, mock(org.springframework.security.core.AuthenticationException.class));

        assertThat(response.getRedirectedUrl()).isEqualTo("https://frontend.example.test/login?error=google_login_failed");
    }

    @Test
    void securityErrorWriterSerializesUnifiedError() throws Exception {
        SecurityErrorWriter writer = new SecurityErrorWriter(new ObjectMapper());
        MockHttpServletResponse response = new MockHttpServletResponse();

        writer.write(response, HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_JWT_TOKEN, "bad token");

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("INVALID_JWT_TOKEN", "bad token");
    }

    @Test
    void vnpayUtilsBuildsQueryStringAndHmac() {
        String query = VnpayUtils.buildQueryString(Map.of(
                "vnp_OrderInfo", "Order 1",
                "empty", "",
                "null", ""));

        assertThat(query).contains("vnp_OrderInfo=Order+1");
        assertThat(VnpayUtils.hmacSHA512("secret", "data")).hasSize(128);
    }

    @Test
    void validatorsAcceptNullOrValidValuesAndRejectInvalidValues() {
        assertThat(new PhoneNumberValidator().isValid("090 000 0000", validationContext())).isTrue();
        assertThat(new PhoneNumberValidator().isValid("123", validationContext())).isFalse();
        assertThat(new PasswordValidator().isValid("abc123", validationContext())).isTrue();
        assertThat(new PasswordValidator().isValid("abcdef", validationContext())).isFalse();
        assertThat(new PriceValidator().isValid(1000.0, validationContext())).isTrue();
        assertThat(new PriceValidator().isValid(-1.0, validationContext())).isFalse();
        assertThat(new PriceValidator().isValid(1_000_000_001.0, validationContext())).isFalse();
    }

    private ConstraintValidatorContext validationContext() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation()).thenReturn(context);
        return context;
    }
}
