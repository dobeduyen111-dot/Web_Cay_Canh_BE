package ceb.exception;

public class InvalidPaymentException extends BadRequestException {

    public InvalidPaymentException() {
        super(ErrorCode.INVALID_PAYMENT);
    }
}
