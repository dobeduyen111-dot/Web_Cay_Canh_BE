package ceb.exception;

public class UnauthorizedException extends DomainException {

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    public UnauthorizedException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
