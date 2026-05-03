package ceb.exception;

public class OrderAccessDeniedException extends UnauthorizedException {

    public OrderAccessDeniedException() {
        super(ErrorCode.ORDER_ACCESS_DENIED);
    }
}
