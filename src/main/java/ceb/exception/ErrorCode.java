package ceb.exception;

public enum ErrorCode {

    UNKNOWN_ERROR("000", "Generic/Technical Error"),
    VALIDATION_ERROR("001", "Invalid required fields: %s"),
    BAD_REQUEST("400", "%s"),
    RESOURCE_NOT_FOUND("404", "%s"),
    UNAUTHORIZED("401", "%s"),

    INVALID_REGISTRATION("100", "Thong tin dang ky khong hop le"),
    EMAIL_REQUIRED("101", "Email khong duoc de trong"),
    PASSWORD_REQUIRED("102", "Mat khau khong duoc de trong"),
    EMAIL_ALREADY_EXISTS("103", "Email da ton tai"),
    PHONE_ALREADY_EXISTS("110", "So dien thoai da ton tai"),
    ACCOUNT_CREATION_FAILED("104", "Khong the tao tai khoan"),
    USER_NOT_FOUND("105", "User khong ton tai"),
    INVALID_CREDENTIALS("106", "Sai ten dang nhap hoac mat khau"),
    AUTHENTICATION_REQUIRED("107", "Nguoi dung chua dang nhap"),
    CURRENT_USER_UNAVAILABLE("108", "Khong lay duoc thong tin nguoi dung hien tai"),
    USER_DISABLED("109", "Tai khoan da bi vo hieu hoa"),
    CURRENT_PASSWORD_INCORRECT("111", "Mat khau hien tai khong dung"),
    PASSWORD_RESET_TOKEN_INVALID("114", "Token dat lai mat khau khong hop le"),
    EMAIL_SEND_FAILED("115", "Khong the gui email xac thuc"),

    PRODUCT_NOT_FOUND("200", "Khong tim thay san pham voi id = %s"),
    INVALID_PRODUCT("201", "Thong tin san pham khong hop le"),
    INVALID_PRODUCT_CATEGORY("202", "Danh muc san pham khong hop le"),
    PRODUCT_NAME_REQUIRED("203", "Ten san pham khong duoc de trong"),
    INVALID_PRODUCT_PRICE("204", "Gia san pham phai lon hon 0"),
    INVALID_PRODUCT_STOCK("205", "So luong ton kho khong hop le"),
    KEYWORD_REQUIRED("206", "Keyword khong duoc de trong"),
    LIMIT_MUST_BE_POSITIVE("207", "Limit phai lon hon 0"),

    CATEGORY_NOT_FOUND("300", "Khong tim thay danh muc voi id = %s"),
    CATEGORY_NAME_REQUIRED("301", "Ten danh muc khong duoc de trong"),

    QUANTITY_MUST_BE_POSITIVE("4001", "So luong phai lon hon 0"),
    CART_ITEM_NOT_FOUND("4002", "Khong tim thay cart item voi id = %s"),
    CART_ITEM_NOT_OWNED("4003", "Cart item khong thuoc user hien tai"),
    EMPTY_CART("4004", "Gio hang dang trong"),

    SHIPPING_ADDRESS_REQUIRED("500", "Dia chi giao hang khong duoc de trong"),
    ORDER_NOT_FOUND("501", "Khong tim thay don hang voi id = %s"),
    ORDER_STATUS_REQUIRED("502", "Trang thai khong duoc de trong"),
    ORDER_ACCESS_DENIED("503", "Ban khong co quyen truy cap don hang nay"),

    INVALID_PAYMENT("600", "Thong tin thanh toan khong hop le"),
    PAYMENT_METHOD_REQUIRED("601", "Phuong thuc thanh toan khong duoc de trong"),
    PAYMENT_NOT_FOUND("602", "Khong tim thay giao dich voi id = %s"),

    ACCESS_DENIED("700", "Ban khong co quyen truy cap tai nguyen nay"),
    JWT_TOKEN_EXPIRED("701", "JWT token da het han"),
    INVALID_JWT_TOKEN("702", "JWT token khong hop le"),
    TOKEN_USER_MISMATCH("703", "Thong tin trong token khong khop voi user trong he thong"),

    FILE_EMPTY("800", "File anh/video khong duoc de trong"),
    FILE_UPLOAD_FAILED("801", "Loi trong qua trinh tai file len Cloudinary");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
