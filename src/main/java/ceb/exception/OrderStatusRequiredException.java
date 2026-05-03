package ceb.exception;

public class OrderStatusRequiredException extends BadRequestException {

    public OrderStatusRequiredException() {
        super(ErrorCode.ORDER_STATUS_REQUIRED);
    }
}
