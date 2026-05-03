package ceb.exception;

public class EmailRequiredException extends BadRequestException {

    public EmailRequiredException() {
        super(ErrorCode.EMAIL_REQUIRED);
    }
}
