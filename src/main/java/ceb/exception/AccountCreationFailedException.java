package ceb.exception;

public class AccountCreationFailedException extends BadRequestException {

    public AccountCreationFailedException() {
        super(ErrorCode.ACCOUNT_CREATION_FAILED);
    }
}
