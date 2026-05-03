package ceb.exception;

public class LimitMustBePositiveException extends BadRequestException {

    public LimitMustBePositiveException() {
        super(ErrorCode.LIMIT_MUST_BE_POSITIVE);
    }
}
