package ceb.exception;

public class BadRequestException extends DomainException {

    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }

    public BadRequestException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
