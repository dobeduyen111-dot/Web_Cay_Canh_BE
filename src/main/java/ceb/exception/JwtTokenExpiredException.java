package ceb.exception;

public class JwtTokenExpiredException extends UnauthorizedException {

    public JwtTokenExpiredException() {
        super(ErrorCode.JWT_TOKEN_EXPIRED);
    }
}
