package ceb.exception;

public class EmptyCartException extends BadRequestException {

    public EmptyCartException() {
        super(ErrorCode.EMPTY_CART);
    }
}
