package ceb.exception;

public class KeywordRequiredException extends BadRequestException {

    public KeywordRequiredException() {
        super(ErrorCode.KEYWORD_REQUIRED);
    }
}
