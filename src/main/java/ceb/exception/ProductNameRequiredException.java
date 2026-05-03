package ceb.exception;

public class ProductNameRequiredException extends BadRequestException {

    public ProductNameRequiredException() {
        super(ErrorCode.PRODUCT_NAME_REQUIRED);
    }
}
