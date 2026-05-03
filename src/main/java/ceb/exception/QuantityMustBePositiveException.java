package ceb.exception;

public class QuantityMustBePositiveException extends BadRequestException {

    public QuantityMustBePositiveException() {
        super(ErrorCode.QUANTITY_MUST_BE_POSITIVE);
    }
}
