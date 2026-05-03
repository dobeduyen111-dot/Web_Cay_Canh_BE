package ceb.controller;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ceb.domain.entity.Users;
import ceb.domain.req.AuthLoginRequest;
import ceb.domain.req.AuthRegisterRequest;
import ceb.domain.req.ForgotPasswordRequest;
import ceb.domain.req.ResetPasswordRequest;
import ceb.domain.req.SendOtpRequest;
import ceb.domain.req.VerifyOtpRequest;
import ceb.domain.res.AuthLoginResponse;
import ceb.domain.res.AuthRegisterResponse;
import ceb.domain.res.LogoutResponse;
import ceb.domain.res.MessageResponse;
import ceb.domain.res.SendOtpResponse;
import ceb.domain.res.UserResponse;
import ceb.exception.DomainException;
import ceb.security.JwtCookieService;
import ceb.security.JwtService;
import ceb.service.service.AuthService;
import ceb.service.service.OtpMailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API dang ky, dang nhap, dang xuat")
public class AuthController {
    @Lazy
    private final AuthService authService;
    @Lazy
    private final JwtService jwtService;
    @Lazy
    private final JwtCookieService jwtCookieService;
    private final OtpMailService otpMailService;

    public AuthController(
            AuthService authService,
            JwtService jwtService,
            JwtCookieService jwtCookieService,
            OtpMailService otpMailService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.jwtCookieService = jwtCookieService;
        this.otpMailService = otpMailService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Dang ky tai khoan moi", description = "Tao tai khoan moi voi email, mat khau, ho ten, so dien thoai, dia chi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Dang ky thanh cong"),
        @ApiResponse(responseCode = "400", description = "Du lieu khong hop le"),
        @ApiResponse(responseCode = "409", description = "Email da ton tai")
    })
    public AuthRegisterResponse register(@Valid @RequestBody AuthRegisterRequest request) {
        Users user = new Users();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        Users createdUser = authService.register(user);
        return new AuthRegisterResponse("Dang ky thanh cong", UserResponse.from(createdUser));
    }

    @PostMapping("/login")
    @Operation(summary = "Dang nhap", description = "Dang nhap bang email va mat khau, JWT duoc gan vao HttpOnly cookie")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dang nhap thanh cong"),
        @ApiResponse(responseCode = "400", description = "Du lieu khong hop le"),
        @ApiResponse(responseCode = "401", description = "Email hoac mat khau khong dung")
    })
    public AuthLoginResponse login(@Valid @RequestBody AuthLoginRequest request, HttpServletResponse response) {
        Users loggedInUser = authService.login(request.getEmail(), request.getPassword());
        String accessToken = jwtService.generateToken(loggedInUser);

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookieService.createAccessTokenCookie(accessToken));
        return new AuthLoginResponse("Dang nhap thanh cong", jwtService.getExpirationMs());
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Quen mat khau", description = "Cap nhat mat khau moi truc tiep theo email ma khong can token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cap nhat mat khau thanh cong"),
        @ApiResponse(responseCode = "400", description = "Du lieu khong hop le")
    })
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request.getEmail(), request.getNewPassword());
        return new MessageResponse("Dat lai mat khau thanh cong");
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Gui OTP qua email", description = "Gui OTP den email, moi lan gui lai phai cach nhau 120 giay va toi da 5 lan resend trong mot phien OTP")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gui OTP thanh cong"),
        @ApiResponse(responseCode = "400", description = "Email khong hop le, chua den thoi gian resend hoac da vuot qua so lan resend")
    })
    public SendOtpResponse sendOtp(@Valid @RequestBody SendOtpRequest request) {
        return otpMailService.sendOtp(request.getEmail());
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Xac thuc OTP", description = "Kiem tra OTP theo email, OTP dung se bi xoa khoi he thong sau khi verify thanh cong")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Xac thuc OTP thanh cong"),
        @ApiResponse(responseCode = "400", description = "OTP sai, OTP het han hoac chua co OTP hop le")
    })
    public MessageResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        otpMailService.verifyOtp(request.getEmail(), request.getOtp());
        return new MessageResponse("Xac thuc OTP thanh cong");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Dat lai mat khau", description = "Cap nhat mat khau moi theo email ma khong can token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dat lai mat khau thanh cong"),
        @ApiResponse(responseCode = "400", description = "Du lieu khong hop le")
    })
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getNewPassword());
        return new MessageResponse("Dat lai mat khau thanh cong");
    }

    @PostMapping("/logout")
    @Operation(summary = "Dang xuat", description = "Xoa JWT HttpOnly cookie tren trinh duyet va blacklist token neu con hop le")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dang xuat thanh cong")
    })
    public LogoutResponse logout(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtCookieService.resolveToken(request).orElse(null);
        if (token != null) {
            try {
                authService.logout(token);
            } catch (DomainException ignored) {
                // Invalid or expired token should not block cookie cleanup.
            }
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookieService.clearAccessTokenCookie());
        response.addHeader(HttpHeaders.SET_COOKIE, clearSessionCookie());
        return new LogoutResponse("Dang xuat thanh cong");
    }

    private String clearSessionCookie() {
        return ResponseCookie.from("JSESSIONID", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build()
                .toString();
    }
}
