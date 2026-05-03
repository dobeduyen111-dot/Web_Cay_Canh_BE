package ceb.exception;

public class ShippingAddressRequiredException extends BadRequestException {

    public ShippingAddressRequiredException() {
        super(ErrorCode.SHIPPING_ADDRESS_REQUIRED);
    }
}
