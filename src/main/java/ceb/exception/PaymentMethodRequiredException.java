package ceb.exception;

public class PaymentMethodRequiredException extends BadRequestException {

    public PaymentMethodRequiredException() {
        super(ErrorCode.PAYMENT_METHOD_REQUIRED);
    }
}
