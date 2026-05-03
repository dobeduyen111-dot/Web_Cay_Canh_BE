package ceb.exception;

public enum VnpayErrorCode {
    INVALID_SIGNATURE("97", "Invalid signature"),
    ORDER_NOT_FOUND("01", "Order not found"),
    INVALID_AMOUNT("04", "Invalid amount"),
    ORDER_ALREADY_PROCESSED("02", "Order already processed"),
    PAYMENT_FAILED("24", "Payment failed");

    private final String rspCode;
    private final String defaultMessage;

    VnpayErrorCode(String rspCode, String defaultMessage) {
        this.rspCode = rspCode;
        this.defaultMessage = defaultMessage;
    }

    public String getRspCode() {
        return rspCode;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
