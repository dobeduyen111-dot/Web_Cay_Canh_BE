package ceb.service.implement;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import ceb.config.VnpayConfig;
import ceb.domain.model.Orders;
import ceb.domain.model.Payments;
import ceb.exception.InvalidPaymentException;
import ceb.exception.PaymentMethodRequiredException;
import ceb.exception.PaymentNotFoundException;
import ceb.exception.VnpayErrorCode;
import ceb.exception.VnpayException;
import ceb.repository.OrdersRepository;
import ceb.repository.PaymentsRepository;
import ceb.service.service.OrderService;
import ceb.service.service.PaymentService;
import ceb.util.VnpayUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private static final String ORDER_STATUS_PENDING = "Cho xu ly";
    private static final String ORDER_STATUS_SUCCESS = "Da thanh toan";
    private static final String ORDER_STATUS_FAILED = "Thanh toan that bai";

    private final PaymentsRepository paymentsRepository;
    private final OrderService orderService;
    private final OrdersRepository ordersRepository;
    private final VnpayConfig vnpayConfig;

    public PaymentServiceImpl(
            PaymentsRepository paymentsRepository,
            OrderService orderService,
            OrdersRepository ordersRepository,
            VnpayConfig vnpayConfig) {
        this.paymentsRepository = paymentsRepository;
        this.orderService = orderService;
        this.ordersRepository = ordersRepository;
        this.vnpayConfig = vnpayConfig;
    }

    @Override
    public Payments create(int userId, Payments payment) {
        if (payment == null || payment.getOrderId() <= 0) {
            throw new InvalidPaymentException();
        }
        if (payment.getPaymentMethod() == null || payment.getPaymentMethod().isBlank()) {
            throw new PaymentMethodRequiredException();
        }

        Orders order = orderService.findByIdForUser(userId, payment.getOrderId());
        if (payment.getAmount() <= 0) {
            payment.setAmount(order.getTotalAmount());
        }

        int paymentId = paymentsRepository.create(payment);
        return findByIdForUser(userId, paymentId);
    }

    @Override
    public Payments findById(int paymentId) {
        try {
            return paymentsRepository.findById(paymentId);
        } catch (Exception ex) {
            throw new PaymentNotFoundException(paymentId);
        }
    }

    @Override
    public List<Payments> findByOrderId(int orderId) {
        orderService.findById(orderId);
        return paymentsRepository.findByOrderId(orderId);
    }

    @Override
    public Payments findByIdForUser(int userId, int paymentId) {
        Payments payment = findById(paymentId);
        orderService.findByIdForUser(userId, payment.getOrderId());
        return payment;
    }

    @Override
    public List<Payments> findByOrderIdForUser(int userId, int orderId) {
        orderService.findByIdForUser(userId, orderId);
        return paymentsRepository.findByOrderId(orderId);
    }

    @Override
    public String createPaymentUrl(long amount, String orderId, String ip) {
        Orders order = findOrderByTxnRef(orderId);

        long expectedAmountMinorUnit = toMinorUnit(order.getTotalAmount());
        long requestAmountMinorUnit = toMinorUnit(amount);
        if (amount <= 0 || requestAmountMinorUnit != expectedAmountMinorUnit) {
            throw new VnpayException(VnpayErrorCode.INVALID_AMOUNT);
        }
        if (!isPending(order.getStatus())) {
            throw new VnpayException(VnpayErrorCode.ORDER_ALREADY_PROCESSED);
        }

        TreeMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpayConfig.getVnpTmnCode());
        params.put("vnp_Amount", String.valueOf(expectedAmountMinorUnit));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderId);
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnpayConfig.getVnpReturnUrl());
        params.put("vnp_IpAddr", ip);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        params.put("vnp_CreateDate", now.format(formatter));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(formatter));

        String queryString = VnpayUtils.buildQueryString(params);
        String secureHash = VnpayUtils.hmacSHA512(vnpayConfig.getVnpHashSecret(), queryString);

        return vnpayConfig.getVnpPayUrl() + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public Map<String, String> processIpn(Map<String, String> params) {
        verifySignature(params);

        String orderId = params.get("vnp_TxnRef");
        Orders order = findOrderByTxnRef(orderId);

        long paidAmountMinorUnit = parseMinorUnitAmount(params.get("vnp_Amount"));
        long expectedAmountMinorUnit = toMinorUnit(order.getTotalAmount());
        if (paidAmountMinorUnit != expectedAmountMinorUnit) {
            throw new VnpayException(VnpayErrorCode.INVALID_AMOUNT);
        }

        synchronized (this) {
            // Idempotent guard: only pending orders can be updated once.
            order = findOrderByTxnRef(orderId);
            if (!isPending(order.getStatus())) {
                throw new VnpayException(VnpayErrorCode.ORDER_ALREADY_PROCESSED);
            }

            boolean success = "00".equals(params.get("vnp_ResponseCode"))
                    && "00".equals(params.get("vnp_TransactionStatus"));

            ordersRepository.updateStatus(order.getOrderId(), success ? ORDER_STATUS_SUCCESS : ORDER_STATUS_FAILED);
        }

        return buildIpnResponse("00", "Confirm Success");
    }

    @Override
    public void verifySignature(Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null || secureHash.isBlank()) {
            log.warn("Missing vnp_SecureHash for request params: {}", params);
            throw new VnpayException(VnpayErrorCode.INVALID_SIGNATURE);
        }

        TreeMap<String, String> signData = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!"vnp_SecureHash".equals(key) && !"vnp_SecureHashType".equals(key)
                    && value != null && !value.isEmpty()) {
                signData.put(key, value);
            }
        }

        String hashData = VnpayUtils.buildQueryString(signData);
        String calculatedHash = VnpayUtils.hmacSHA512(vnpayConfig.getVnpHashSecret(), hashData);
        if (!calculatedHash.equalsIgnoreCase(secureHash)) {
            log.warn("Invalid VNPAY signature. expected={}, actual={}, data={}", calculatedHash, secureHash, hashData);
            throw new VnpayException(VnpayErrorCode.INVALID_SIGNATURE);
        }
    }

    @Override
    public Map<String, Object> processReturn(Map<String, String> params) {
        verifySignature(params);

        String responseCode = params.getOrDefault("vnp_ResponseCode", "");
        boolean success = "00".equals(responseCode);
        if (!success) {
            throw new VnpayException(VnpayErrorCode.PAYMENT_FAILED, "Payment failed with code: " + responseCode);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", params.get("vnp_TxnRef"));
        result.put("amount", parseMinorUnitAmount(params.get("vnp_Amount")) / 100);
        result.put("transactionNo", params.get("vnp_TransactionNo"));
        result.put("responseCode", responseCode);
        result.put("message", "Payment success, waiting for IPN confirmation");
        result.put("success", true);
        return result;
    }

    private Map<String, String> buildIpnResponse(String rspCode, String message) {
        Map<String, String> result = new HashMap<>();
        result.put("RspCode", rspCode);
        result.put("Message", message);
        return result;
    }

    private long parseMinorUnitAmount(String amountRaw) {
        try {
            return Long.parseLong(amountRaw);
        } catch (Exception ex) {
            throw new VnpayException(VnpayErrorCode.INVALID_AMOUNT);
        }
    }

    private Orders findOrderByTxnRef(String txnRef) {
        try {
            int orderId = Integer.parseInt(txnRef);
            return ordersRepository.findById(orderId);
        } catch (Exception ex) {
            throw new VnpayException(VnpayErrorCode.ORDER_NOT_FOUND);
        }
    }

    private boolean isPending(String status) {
        return status != null && ORDER_STATUS_PENDING.equalsIgnoreCase(status.trim());
    }

    private long toMinorUnit(double amount) {
        // VNPAY expects amount in smallest unit (value * 100), numeric only.
        return BigDecimal.valueOf(amount)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    private long toMinorUnit(long amount) {
        return amount * 100;
    }
}
