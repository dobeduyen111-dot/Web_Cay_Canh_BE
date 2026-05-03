package ceb.exception;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
