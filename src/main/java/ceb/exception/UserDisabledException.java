package ceb.exception;

public class UserDisabledException extends UnauthorizedException {

    public UserDisabledException() {
        super(ErrorCode.USER_DISABLED);
    }
}
