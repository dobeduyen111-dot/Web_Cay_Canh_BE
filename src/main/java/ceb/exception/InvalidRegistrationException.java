package ceb.exception;

public class InvalidRegistrationException extends BadRequestException {

    public InvalidRegistrationException() {
        super(ErrorCode.INVALID_REGISTRATION);
    }
}
