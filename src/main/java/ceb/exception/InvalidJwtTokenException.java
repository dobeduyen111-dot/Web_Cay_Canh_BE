package ceb.exception;

public class InvalidJwtTokenException extends UnauthorizedException {

    public InvalidJwtTokenException() {
        super(ErrorCode.INVALID_JWT_TOKEN);
    }
}
