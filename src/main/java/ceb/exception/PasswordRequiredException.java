package ceb.exception;

public class PasswordRequiredException extends BadRequestException {

    public PasswordRequiredException() {
        super(ErrorCode.PASSWORD_REQUIRED);
    }
}
