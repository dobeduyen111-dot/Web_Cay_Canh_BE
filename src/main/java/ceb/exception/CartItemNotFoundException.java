package ceb.exception;

public class CartItemNotFoundException extends ResourceNotFoundException {

    public CartItemNotFoundException(int cartItemId) {
        super(ErrorCode.CART_ITEM_NOT_FOUND, cartItemId);
    }
}
