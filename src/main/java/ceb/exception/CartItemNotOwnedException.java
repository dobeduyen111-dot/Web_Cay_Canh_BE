package ceb.exception;

public class CartItemNotOwnedException extends BadRequestException {

    public CartItemNotOwnedException() {
        super(ErrorCode.CART_ITEM_NOT_OWNED);
    }
}
