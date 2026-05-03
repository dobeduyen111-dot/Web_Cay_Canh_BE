package ceb.exception;

public class CategoryNameRequiredException extends BadRequestException {

    public CategoryNameRequiredException() {
        super(ErrorCode.CATEGORY_NAME_REQUIRED);
    }
}
