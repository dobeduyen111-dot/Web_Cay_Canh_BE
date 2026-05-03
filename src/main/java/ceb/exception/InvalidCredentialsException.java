package ceb.exception;

public class InvalidCredentialsException extends UnauthorizedException {

    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}
