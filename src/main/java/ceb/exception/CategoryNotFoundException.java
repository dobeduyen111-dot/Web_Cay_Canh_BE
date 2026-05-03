package ceb.exception;

public class CategoryNotFoundException extends ResourceNotFoundException {

    public CategoryNotFoundException(int categoryId) {
        super(ErrorCode.CATEGORY_NOT_FOUND, categoryId);
    }
}
