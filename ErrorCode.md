# Error Code

Tai lieu nay tong hop cac ma loi duoc khai bao trong:

- `src/main/java/ceb/exception/ErrorCode.java`
- `src/main/java/ceb/exception/VnpayErrorCode.java`

## 1. ErrorCode

### Nhom chung

| Enum | Code | Default message | Ghi chu |
|---|---|---|---|
| `UNKNOWN_ERROR` | `000` | `Generic/Technical Error` | Loi ky thuat chung, fallback cho exception khong duoc xu ly rieng |
| `VALIDATION_ERROR` | `001` | `Invalid required fields: %s` | `%s` se duoc thay bang noi dung validation cu the |
| `BAD_REQUEST` | `400` | `%s` | `%s` la message dong tu exception, vi du `Mat khau moi khong duoc trung mat khau cu` |
| `RESOURCE_NOT_FOUND` | `404` | `%s` | `%s` la message tim khong thay tai nguyen |
| `UNAUTHORIZED` | `401` | `%s` | `%s` la message uy quyen/xac thuc |

### Nhom auth va user

| Enum | Code | Default message |
|---|---|---|
| `INVALID_REGISTRATION` | `100` | `Thong tin dang ky khong hop le` |
| `EMAIL_REQUIRED` | `101` | `Email khong duoc de trong` |
| `PASSWORD_REQUIRED` | `102` | `Mat khau khong duoc de trong` |
| `EMAIL_ALREADY_EXISTS` | `103` | `Email da ton tai` |
| `ACCOUNT_CREATION_FAILED` | `104` | `Khong the tao tai khoan` |
| `USER_NOT_FOUND` | `105` | `User khong ton tai` |
| `INVALID_CREDENTIALS` | `106` | `Sai ten dang nhap hoac mat khau` |
| `AUTHENTICATION_REQUIRED` | `107` | `Nguoi dung chua dang nhap` |
| `CURRENT_USER_UNAVAILABLE` | `108` | `Khong lay duoc thong tin nguoi dung hien tai` |
| `USER_DISABLED` | `109` | `Tai khoan da bi vo hieu hoa` |
| `PHONE_ALREADY_EXISTS` | `110` | `So dien thoai da ton tai` |
| `CURRENT_PASSWORD_INCORRECT` | `111` | `Mat khau hien tai khong dung` |
| `PASSWORD_RESET_TOKEN_INVALID` | `114` | `Token dat lai mat khau khong hop le` |
| `EMAIL_SEND_FAILED` | `115` | `Khong the gui email xac thuc` |

### Nhom product

| Enum | Code | Default message |
|---|---|---|
| `PRODUCT_NOT_FOUND` | `200` | `Khong tim thay san pham voi id = %s` |
| `INVALID_PRODUCT` | `201` | `Thong tin san pham khong hop le` |
| `INVALID_PRODUCT_CATEGORY` | `202` | `Danh muc san pham khong hop le` |
| `PRODUCT_NAME_REQUIRED` | `203` | `Ten san pham khong duoc de trong` |
| `INVALID_PRODUCT_PRICE` | `204` | `Gia san pham phai lon hon 0` |
| `INVALID_PRODUCT_STOCK` | `205` | `So luong ton kho khong hop le` |
| `KEYWORD_REQUIRED` | `206` | `Keyword khong duoc de trong` |
| `LIMIT_MUST_BE_POSITIVE` | `207` | `Limit phai lon hon 0` |

### Nhom category

| Enum | Code | Default message |
|---|---|---|
| `CATEGORY_NOT_FOUND` | `300` | `Khong tim thay danh muc voi id = %s` |
| `CATEGORY_NAME_REQUIRED` | `301` | `Ten danh muc khong duoc de trong` |

### Nhom cart

| Enum | Code | Default message |
|---|---|---|
| `QUANTITY_MUST_BE_POSITIVE` | `4001` | `So luong phai lon hon 0` |
| `CART_ITEM_NOT_FOUND` | `4002` | `Khong tim thay cart item voi id = %s` |
| `CART_ITEM_NOT_OWNED` | `4003` | `Cart item khong thuoc user hien tai` |
| `EMPTY_CART` | `4004` | `Gio hang dang trong` |

### Nhom order

| Enum | Code | Default message |
|---|---|---|
| `SHIPPING_ADDRESS_REQUIRED` | `500` | `Dia chi giao hang khong duoc de trong` |
| `ORDER_NOT_FOUND` | `501` | `Khong tim thay don hang voi id = %s` |
| `ORDER_STATUS_REQUIRED` | `502` | `Trang thai khong duoc de trong` |
| `ORDER_ACCESS_DENIED` | `503` | `Ban khong co quyen truy cap don hang nay` |

### Nhom payment

| Enum | Code | Default message |
|---|---|---|
| `INVALID_PAYMENT` | `600` | `Thong tin thanh toan khong hop le` |
| `PAYMENT_METHOD_REQUIRED` | `601` | `Phuong thuc thanh toan khong duoc de trong` |
| `PAYMENT_NOT_FOUND` | `602` | `Khong tim thay giao dich voi id = %s` |

### Nhom security / JWT

| Enum | Code | Default message |
|---|---|---|
| `ACCESS_DENIED` | `700` | `Ban khong co quyen truy cap tai nguyen nay` |
| `JWT_TOKEN_EXPIRED` | `701` | `JWT token da het han` |
| `INVALID_JWT_TOKEN` | `702` | `JWT token khong hop le` |
| `TOKEN_USER_MISMATCH` | `703` | `Thong tin trong token khong khop voi user trong he thong` |

### Nhom file / Cloudinary

| Enum | Code | Default message |
|---|---|---|
| `FILE_EMPTY` | `800` | `File anh/video khong duoc de trong` |
| `FILE_UPLOAD_FAILED` | `801` | `Loi trong qua trinh tai file len Cloudinary` |

## 2. VnpayErrorCode

Day la bo ma loi rieng cho VNPay, khong dung chung format `ApiError.errors[]` trong moi truong hop.

| Enum | RspCode | Default message | Ghi chu |
|---|---|---|---|
| `INVALID_SIGNATURE` | `97` | `Invalid signature` | Sai chu ky request/response VNPay |
| `ORDER_NOT_FOUND` | `01` | `Order not found` | Khong parse duoc `orderId` hoac khong tim thay order |
| `ORDER_ALREADY_PROCESSED` | `02` | `Order already processed` | Order khong con o trang thai `Cho xu ly` |
| `INVALID_AMOUNT` | `04` | `Invalid amount` | So tien gui len khong khop tong tien order |
| `PAYMENT_FAILED` | `24` | `Payment failed` | Thanh toan tra ve ma that bai |

## 3. Ghi chu su dung

- Flow doi/reset mat khau hien tai khong con dung reset token.
- Khi chua dang nhap, API quen mat khau/reset mat khau tra `BAD_REQUEST (400)` neu `newPassword` trung mat khau cu, va `RESOURCE_NOT_FOUND (404)` neu email khong ton tai.
- Khi da dang nhap, API doi mat khau tra `CURRENT_PASSWORD_INCORRECT (111)` neu `oldPassword` sai, va `BAD_REQUEST (400)` neu `newPassword` trung mat khau cu.
- `PASSWORD_RESET_TOKEN_INVALID` hien van duoc khai bao trong enum, nhung khong con nam trong flow doi/reset mat khau hien tai.

- Cac message co `%s` la message template. Khi tra ve thuc te, backend se format gia tri cu the vao message.
- `ErrorCode` thuong duoc tra ve theo format:

```json
{
  "errors": [
    {
      "unifiedErrorCode": "404",
      "errorCode": "RESOURCE_NOT_FOUND",
      "errorMessage": "Khong tim thay san pham voi id = 10"
    }
  ]
}
```

- `VnpayErrorCode` co the duoc tra ve theo 2 kieu tuy endpoint:

```json
{
  "errorCode": "PAYMENT_FAILED",
  "message": "Payment failed with code: 24"
}
```

hoac:

```json
{
  "RspCode": "97",
  "Message": "Invalid signature"
}
```
