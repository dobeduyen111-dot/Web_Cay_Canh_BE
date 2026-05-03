package ceb.exception;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(int productId) {
        super(ErrorCode.PRODUCT_NOT_FOUND, productId);
    }
}
