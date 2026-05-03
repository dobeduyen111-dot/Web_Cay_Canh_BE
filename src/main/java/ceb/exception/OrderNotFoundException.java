package ceb.exception;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(int orderId) {
        super(ErrorCode.ORDER_NOT_FOUND, orderId);
    }
}
