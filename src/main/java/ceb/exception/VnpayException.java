package ceb.exception;

public class VnpayException extends RuntimeException {

    private final VnpayErrorCode errorCode;

    public VnpayException(VnpayErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public VnpayException(VnpayErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public VnpayErrorCode getErrorCode() {
        return errorCode;
    }
}
