package ceb.domain.res;

import java.util.Date;

import ceb.domain.model.Payments;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private int paymentId;
    private int orderId;
    private String paymentMethod;
    private double amount;
    private Date paymentDate;
    private boolean successful;

    public static PaymentResponse from(Payments payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getPaymentMethod(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.isSuccessful());
    }
}
