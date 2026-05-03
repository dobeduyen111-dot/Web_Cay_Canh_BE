package ceb.service.service;

import java.util.List;
import java.util.Map;

import ceb.domain.model.Payments;

public interface PaymentService {

    Payments create(int userId, Payments payment);

    Payments findById(int paymentId);

    Payments findByIdForUser(int userId, int paymentId);

    List<Payments> findByOrderId(int orderId);

    List<Payments> findByOrderIdForUser(int userId, int orderId);

    String createPaymentUrl(long amount, String orderId, String ip);

    Map<String, String> processIpn(Map<String, String> params);

    void verifySignature(Map<String, String> params);

    Map<String, Object> processReturn(Map<String, String> params);
}
