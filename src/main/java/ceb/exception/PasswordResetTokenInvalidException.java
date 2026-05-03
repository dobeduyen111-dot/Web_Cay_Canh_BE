package ceb.exception;

public class PasswordResetTokenInvalidException extends BadRequestException {

    public PasswordResetTokenInvalidException() {
        super(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
    }
}
