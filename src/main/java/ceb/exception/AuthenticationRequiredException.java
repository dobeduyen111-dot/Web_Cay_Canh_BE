package ceb.exception;

public class AuthenticationRequiredException extends UnauthorizedException {

    public AuthenticationRequiredException() {
        super(ErrorCode.AUTHENTICATION_REQUIRED);
    }
}
