package ceb.exception;

public class PaymentNotFoundException extends ResourceNotFoundException {

    public PaymentNotFoundException(int paymentId) {
        super(ErrorCode.PAYMENT_NOT_FOUND, paymentId);
    }
}
