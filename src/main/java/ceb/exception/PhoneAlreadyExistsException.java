package ceb.exception;

public class PhoneAlreadyExistsException extends BadRequestException {

    public PhoneAlreadyExistsException() {
        super(ErrorCode.PHONE_ALREADY_EXISTS);
    }
}
