package ceb.exception;

public class InvalidProductStockException extends BadRequestException {

    public InvalidProductStockException() {
        super(ErrorCode.INVALID_PRODUCT_STOCK);
    }
}
