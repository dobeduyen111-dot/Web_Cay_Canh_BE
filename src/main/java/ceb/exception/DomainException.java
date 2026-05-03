package ceb.exception;

import java.util.IllegalFormatException;

public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    private final transient Object[] args;

    public DomainException(ErrorCode errorCode, Object... args) {
        super(formatMessage(errorCode, args));
        this.errorCode = errorCode;
        this.args = args;
    }

    public DomainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = null;
    }

    public DomainException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.args = null;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getArgs() {
        return args;
    }

    private static String formatMessage(ErrorCode errorCode, Object... args) {
        String template = errorCode.getMessage();
        if (args == null || args.length == 0) {
            return template;
        }

        try {
            return String.format(template, args);
        } catch (IllegalFormatException ex) {
            if (args.length == 1) {
                return String.valueOf(args[0]);
            }
            return template;
        }
    }
}
