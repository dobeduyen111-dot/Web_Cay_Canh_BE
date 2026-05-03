package ceb.exception;

public class CurrentUserUnavailableException extends UnauthorizedException {

    public CurrentUserUnavailableException() {
        super(ErrorCode.CURRENT_USER_UNAVAILABLE);
    }
}
