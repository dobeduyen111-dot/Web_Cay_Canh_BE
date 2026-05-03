package ceb.exception;

public class TokenUserMismatchException extends UnauthorizedException {

    public TokenUserMismatchException() {
        super(ErrorCode.TOKEN_USER_MISMATCH);
    }
}
