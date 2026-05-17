package ceb.controller;

import java.util.List;
import java.util.Map;

import ceb.TestData;
import ceb.domain.entity.Users;
import ceb.domain.req.AdminCreateUserRequest;
import ceb.domain.req.AdminUpdateOrderStatusRequest;
import ceb.domain.req.AdminUpdatePasswordRequest;
import ceb.domain.req.AuthLoginRequest;
import ceb.domain.req.AuthRegisterRequest;
import ceb.domain.req.ForgotPasswordRequest;
import ceb.domain.req.ResetPasswordRequest;
import ceb.domain.req.SendOtpRequest;
import ceb.domain.req.VerifyOtpRequest;
import ceb.domain.res.DashboardResponse;
import ceb.domain.res.SendOtpResponse;
import ceb.exception.BadRequestException;
import ceb.exception.VnpayErrorCode;
import ceb.exception.VnpayException;
import ceb.security.JwtCookieService;
import ceb.security.JwtService;
import ceb.service.service.AuthService;
import ceb.service.service.DashboardService;
import ceb.service.service.OrderService;
import ceb.service.service.OtpMailService;
import ceb.service.service.UsersService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthAdminExceptionControllerTest {

    @Test
    void authControllerHandlesRegisterLoginPasswordOtpAndLogout() {
        AuthService authService = mock(AuthService.class);
        JwtService jwtService = mock(JwtService.class);
        JwtCookieService jwtCookieService = mock(JwtCookieService.class);
        OtpMailService otpMailService = mock(OtpMailService.class);
        AuthController controller = new AuthController(authService, jwtService, jwtCookieService, otpMailService);
        Users user = TestData.user();
        when(authService.register(any(Users.class))).thenReturn(user);
        when(authService.login("a@example.com", "abc123")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt");
        when(jwtService.getExpirationMs()).thenReturn(60_000L);
        when(jwtCookieService.createAccessTokenCookie("jwt")).thenReturn("access_token=jwt; HttpOnly");
        when(jwtCookieService.resolveToken(any())).thenReturn(java.util.Optional.of("jwt"));
        when(jwtCookieService.clearAccessTokenCookie()).thenReturn("access_token=; Max-Age=0");
        when(otpMailService.sendOtp("a@example.com")).thenReturn(new SendOtpResponse("sent", 300, 120, 4));

        assertThat(controller.register(new AuthRegisterRequest("A", "a@example.com", "abc123", "0900000000", "Da Lat"))
                .getUser().getEmail()).isEqualTo("a@example.com");

        MockHttpServletResponse loginResponse = new MockHttpServletResponse();
        assertThat(controller.login(new AuthLoginRequest("a@example.com", "abc123"), loginResponse).getTtl())
                .isEqualTo(60_000L);
        assertThat(loginResponse.getHeader(HttpHeaders.SET_COOKIE)).contains("access_token=jwt");

        assertThat(controller.forgotPassword(new ForgotPasswordRequest("a@example.com", "new123")).getMessage()).contains("Dat lai");
        assertThat(controller.sendOtp(new SendOtpRequest("a@example.com")).getRemainingResends()).isEqualTo(4);
        assertThat(controller.verifyOtp(new VerifyOtpRequest("a@example.com", "123456")).getMessage()).contains("Xac thuc");
        assertThat(controller.resetPassword(new ResetPasswordRequest("a@example.com", "new123")).getMessage()).contains("Dat lai");

        MockHttpServletRequest logoutRequest = new MockHttpServletRequest();
        logoutRequest.setCookies(new Cookie("access_token", "jwt"));
        MockHttpSession session = new MockHttpSession();
        logoutRequest.setSession(session);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("a@example.com", null));
        MockHttpServletResponse logoutResponse = new MockHttpServletResponse();
        assertThat(controller.logout(logoutRequest, logoutResponse).getMessage()).contains("Dang xuat");
        verify(authService).logout("jwt");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(session.isInvalid()).isTrue();
        assertThat(logoutResponse.getHeaders(HttpHeaders.SET_COOKIE))
                .anySatisfy(header -> assertThat(header).contains("access_token=", "Max-Age=0"))
                .anySatisfy(header -> assertThat(header).contains("JSESSIONID=", "Max-Age=0", "Path=/"));
    }

    @Test
    void adminControllerDelegatesManagementOperations() {
        UsersService usersService = mock(UsersService.class);
        OrderService orderService = mock(OrderService.class);
        DashboardService dashboardService = mock(DashboardService.class);
        AuthService authService = mock(AuthService.class);
        AdminController controller = new AdminController(usersService, orderService, dashboardService, authService);
        when(dashboardService.getDashboard()).thenReturn(new DashboardResponse(3, 500_000, 2));
        when(orderService.findAll()).thenReturn(List.of(TestData.order()));
        when(orderService.updateStatus(17, "Da giao")).thenReturn(TestData.order());
        when(authService.register(any(Users.class))).thenReturn(TestData.user());

        assertThat(controller.getDashboard().getTotalOrders()).isEqualTo(3);
        assertThat(controller.getAllOrders()).hasSize(1);
        assertThat(controller.updateOrderStatus(17, new AdminUpdateOrderStatusRequest("Da giao")).getOrderId()).isEqualTo(17);
        assertThat(controller.updateUserPassword(7, new AdminUpdatePasswordRequest("new123")).getMessage()).contains("Update");
        assertThat(controller.deleteUser(7).getMessage()).contains("Xoa");
        assertThat(controller.deleteOrder(17).getMessage()).contains("Xoa");
        assertThat(controller.createUser(new AdminCreateUserRequest(
                "Admin",
                "admin@example.com",
                "abc123",
                "0900000000",
                "Da Lat",
                "ADMIN")).getUser().getEmail()).isEqualTo("a@example.com");
    }

    @Test
    void globalVnpayExceptionHandlerReturnsBadRequestPayload() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        var response = handler.handleVnpayException(new VnpayException(VnpayErrorCode.INVALID_AMOUNT));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("errorCode", "INVALID_AMOUNT");
    }

    @Test
    void paymentControllerIpnConvertsVnpayAndGenericErrors() {
        ceb.service.service.PaymentService paymentService = mock(ceb.service.service.PaymentService.class);
        PaymentController controller = new PaymentController(paymentService, mock(ceb.service.service.CurrentUserService.class));
        when(paymentService.processIpn(Map.of("bad", "1")))
                .thenThrow(new VnpayException(VnpayErrorCode.INVALID_SIGNATURE));
        when(paymentService.processIpn(Map.of("boom", "1")))
                .thenThrow(new RuntimeException("boom"));

        assertThat(controller.vnpayIpn(Map.of("bad", "1")).get("RspCode")).isEqualTo("97");
        assertThat(controller.vnpayIpn(Map.of("boom", "1")).get("RspCode")).isEqualTo("99");
    }

    @Test
    void apiExceptionHandlerMapsDomainExceptions() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThat(handler.handleBadRequest(request, new BadRequestException("bad")).getStatusCode().value()).isEqualTo(400);
    }
}
