package ceb.exception;

public class CurrentPasswordIncorrectException extends BadRequestException {

    public CurrentPasswordIncorrectException() {
        super(ErrorCode.CURRENT_PASSWORD_INCORRECT);
    }
}
