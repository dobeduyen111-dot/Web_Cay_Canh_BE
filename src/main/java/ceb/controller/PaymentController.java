package ceb.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ceb.domain.model.Payments;
import ceb.domain.req.PaymentRequest;
import ceb.domain.req.VnpayCreatePaymentRequest;
import ceb.domain.res.PaymentCreateResponse;
import ceb.domain.res.PaymentResponse;
import ceb.exception.VnpayException;
import ceb.service.service.CurrentUserService;
import ceb.service.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@Validated
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentUserService currentUserService;

    public PaymentController(
            PaymentService paymentService,
            CurrentUserService currentUserService) {
        this.paymentService = paymentService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentCreateResponse create(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request) {
        int userId = currentUserService.getCurrentUserId(authentication);

        Payments payment = new Payments();
        payment.setOrderId(request.getOrderId());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(request.getAmount() == null ? 0 : request.getAmount());
        payment.setSuccessful(Boolean.TRUE.equals(request.getSuccessful()));

        Payments createdPayment = paymentService.create(userId, payment);
        return new PaymentCreateResponse("Tao thanh toan thanh cong", PaymentResponse.from(createdPayment));
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getById(
            Authentication authentication,
            @PathVariable @Positive(message = "Payment id phai lon hon 0") int paymentId) {
        int userId = currentUserService.getCurrentUserId(authentication);
        return PaymentResponse.from(paymentService.findByIdForUser(userId, paymentId));
    }

    @GetMapping("/order/{orderId}")
    public List<PaymentResponse> getByOrderId(
            Authentication authentication,
            @PathVariable @Positive(message = "Order id phai lon hon 0") int orderId) {
        int userId = currentUserService.getCurrentUserId(authentication);
        return paymentService.findByOrderIdForUser(userId, orderId).stream().map(PaymentResponse::from).toList();
    }

    @PostMapping("/create")
    public Map<String, String> createVnpayUrl(
            @Valid @RequestBody VnpayCreatePaymentRequest request,
            HttpServletRequest httpServletRequest) {
        String ip = resolveClientIp(httpServletRequest);
        String paymentUrl = paymentService.createPaymentUrl(request.getAmount(), request.getOrderId(), ip);
        return Map.of("paymentUrl", paymentUrl);
    }

    @GetMapping("/vnpay-return")
    public Map<String, Object> vnpayReturn(@RequestParam Map<String, String> params) {
        // Return URL only displays payment result, no DB update here.
        return paymentService.processReturn(params);
    }

    @GetMapping("/vnpay-ipn")
    public Map<String, String> vnpayIpn(@RequestParam Map<String, String> params) {
        try {
            return paymentService.processIpn(params);
        } catch (VnpayException ex) {
            return Map.of(
                    "RspCode", ex.getErrorCode().getRspCode(),
                    "Message", ex.getMessage());
        } catch (Exception ex) {
            Map<String, String> result = new HashMap<>();
            result.put("RspCode", "99");
            result.put("Message", "Unknown error");
            return result;
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
