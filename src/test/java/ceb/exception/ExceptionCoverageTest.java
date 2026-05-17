package ceb.exception;

import java.lang.reflect.Constructor;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionCoverageTest {

    @Test
    void domainExceptionFormatsMessagesAndStoresErrorCode() {
        DomainException formatted = new DomainException(ErrorCode.PRODUCT_NOT_FOUND, 11);
        assertThat(formatted.getMessage()).contains("11");
        assertThat(formatted.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        assertThat(formatted.getArgs()).containsExactly(11);

        DomainException fallback = new DomainException(ErrorCode.BAD_REQUEST, "plain message");
        assertThat(fallback.getMessage()).isEqualTo("plain message");

        DomainException withCause = new DomainException(ErrorCode.EMAIL_SEND_FAILED, new RuntimeException("mail"));
        assertThat(withCause.getCause()).hasMessage("mail");
    }

    @Test
    void concreteDomainExceptionsCanBeConstructed() {
        List.of(
                new AccountCreationFailedException(),
                new AuthenticationRequiredException(),
                new CartItemNotFoundException(5),
                new CartItemNotOwnedException(),
                new CategoryNameRequiredException(),
                new CategoryNotFoundException(1),
                new CurrentPasswordIncorrectException(),
                new CurrentUserUnavailableException(),
                new EmailAlreadyExistsException(),
                new EmailRequiredException(),
                new EmailSendFailedException(new RuntimeException("mail")),
                new EmptyCartException(),
                new FileEmptyException(),
                new FileUploadFailedException(new RuntimeException("upload")),
                new InvalidCredentialsException(),
                new InvalidJwtTokenException(),
                new InvalidPaymentException(),
                new InvalidProductCategoryException(),
                new InvalidProductException(),
                new InvalidProductPriceException(),
                new InvalidProductStockException(),
                new InvalidRegistrationException(),
                new JwtTokenExpiredException(),
                new KeywordRequiredException(),
                new LimitMustBePositiveException(),
                new OrderAccessDeniedException(),
                new OrderNotFoundException(17),
                new OrderStatusRequiredException(),
                new PasswordRequiredException(),
                new PasswordResetTokenInvalidException(),
                new PaymentMethodRequiredException(),
                new PaymentNotFoundException(23),
                new PhoneAlreadyExistsException(),
                new ProductNameRequiredException(),
                new ProductNotFoundException(11),
                new QuantityMustBePositiveException(),
                new ShippingAddressRequiredException(),
                new TokenUserMismatchException(),
                new UnauthorizedException(ErrorCode.UNAUTHORIZED),
                new UserDisabledException(),
                new UserNotFoundException()
        ).forEach(exception -> assertThat(exception.getMessage()).isNotBlank());
    }

    @Test
    void vnpayExceptionConstructorsExposeErrorCodeAndCustomMessage() {
        VnpayException defaultMessage = new VnpayException(VnpayErrorCode.INVALID_AMOUNT);
        VnpayException customMessage = new VnpayException(VnpayErrorCode.PAYMENT_FAILED, "failed");

        assertThat(defaultMessage.getErrorCode()).isEqualTo(VnpayErrorCode.INVALID_AMOUNT);
        assertThat(customMessage.getMessage()).isEqualTo("failed");
        assertThat(VnpayErrorCode.INVALID_SIGNATURE.getRspCode()).isEqualTo("97");
    }

    @Test
    void exceptionHandlerBaseBuildsResponsesAndLogsSafely() {
        TestHandler handler = new TestHandler();
        handler.objectMapper = new ObjectMapper();
        ApiErrorEntry entry = handler.entry(ErrorCode.BAD_REQUEST, "bad");

        assertThat(handler.response(HttpStatus.BAD_REQUEST, new BadRequestException("bad")).getBody().getErrors())
                .extracting(ApiErrorEntry::getErrorCode)
                .containsExactly("BAD_REQUEST");
        assertThat(handler.response(HttpStatus.BAD_REQUEST, List.of(entry)).getBody().getErrors()).containsExactly(entry);
        assertThat(handler.response(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "bad").getStatusCode().value()).isEqualTo(400);
        assertThat(handler.json(List.of(entry))).contains("BAD_REQUEST");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        handler.log(request, List.of(entry));
    }

    @Test
    void allErrorCodesExposeCodeAndMessage() {
        assertThat(ErrorCode.values()).allSatisfy(errorCode -> {
            assertThat(errorCode.getCode()).isNotBlank();
            assertThat(errorCode.getMessage()).isNotBlank();
        });
    }

    @SuppressWarnings("unused")
    private Object instantiate(Constructor<?> constructor) throws Exception {
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static class TestHandler extends ExceptionHandlerBase {
        ResponseEntity<ApiError> response(HttpStatus status, DomainException exception) {
            return buildResponse(status, exception);
        }

        ResponseEntity<ApiError> response(HttpStatus status, ErrorCode errorCode, String message) {
            return buildResponse(status, errorCode, message);
        }

        ResponseEntity<ApiError> response(HttpStatus status, List<ApiErrorEntry> entries) {
            return buildResponse(status, entries);
        }

        ApiErrorEntry entry(ErrorCode errorCode, String message) {
            return toApiErrorEntry(errorCode, message);
        }

        String json(List<ApiErrorEntry> entries) {
            return parseApiErrorToJsonString(entries);
        }

        void log(MockHttpServletRequest request, List<ApiErrorEntry> entries) {
            logError("test", request, entries, null);
        }

    }
}
