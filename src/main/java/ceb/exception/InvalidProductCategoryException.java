package ceb.exception;

public class InvalidProductCategoryException extends BadRequestException {

    public InvalidProductCategoryException() {
        super(ErrorCode.INVALID_PRODUCT_CATEGORY);
    }
}
