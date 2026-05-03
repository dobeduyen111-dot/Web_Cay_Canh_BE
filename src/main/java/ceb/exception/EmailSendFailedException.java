package ceb.exception;

public class EmailSendFailedException extends BadRequestException {

    public EmailSendFailedException(Throwable cause) {
        super(ErrorCode.EMAIL_SEND_FAILED);
        initCause(cause);
    }
}
