package ceb.exception;

public class FileEmptyException extends BadRequestException {

    public FileEmptyException() {
        super(ErrorCode.FILE_EMPTY);
    }
}
