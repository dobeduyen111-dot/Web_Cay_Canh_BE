package ceb.exception;

public class InvalidProductPriceException extends BadRequestException {

    public InvalidProductPriceException() {
        super(ErrorCode.INVALID_PRODUCT_PRICE);
    }
}
