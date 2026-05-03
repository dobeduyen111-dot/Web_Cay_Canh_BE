package ceb.exception;

public class EmailAlreadyExistsException extends BadRequestException {

    public EmailAlreadyExistsException() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
}
