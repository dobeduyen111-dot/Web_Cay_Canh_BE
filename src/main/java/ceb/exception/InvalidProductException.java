package ceb.exception;

public class InvalidProductException extends BadRequestException {

    public InvalidProductException() {
        super(ErrorCode.INVALID_PRODUCT);
    }
}
