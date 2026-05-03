package ceb.exception;

public class FileUploadFailedException extends DomainException {

    public FileUploadFailedException(Throwable cause) {
        super(ErrorCode.FILE_UPLOAD_FAILED, cause);
    }
}
