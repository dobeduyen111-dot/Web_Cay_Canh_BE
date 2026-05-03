# API Backend Cay Canh

Tai lieu nay duoc thong ke lai tu code backend hien co trong `src/main/java/ceb/controller`, DTO request/response, validation, security config va service logic.

## Tong quan

- Tong so endpoint hien co: `49`
- Base path API: `/api`
- Swagger dang mo theo config:
  - `/swagger-ui.html`
  - `/v3/api-docs`
- Cac endpoint thuc te duoc liet ke ben duoi la nhung endpoint co controller implement. Trong `SecurityConfig` co whitelist `/api/view/products/**` nhung hien khong thay controller tuong ung.

## Co che auth

- Public:
  - `/api/auth/**`
  - `/oauth2/**`
  - `/login/oauth2/**`
  - `/api/home/**`
  - `GET /api/products/**`
  - `GET /api/categories/**`
  - `/api/payment/create`
  - `/api/payment/vnpay-return`
  - `/api/payment/vnpay-ipn`
- Can dang nhap:
  - `/api/cart/**`
  - `/api/checkout/**`
  - `/api/orders/**`
  - `/api/payment/**` tru 3 endpoint public o tren
  - `/api/wishlist/**`
  - `/api/user/**`
- Chi `ADMIN`:
  - `/api/admin/**`
  - `POST/PUT/DELETE /api/products/**`
  - `POST/PUT/DELETE /api/categories/**`

### JWT hien tai

- Login sinh JWT va tra ve qua cookie HttpOnly.
- Cookie mac dinh theo `application.properties`:
  - `name = access_token`
  - `path = /api`
  - `domain = localhost`
  - `secure = true`
  - `sameSite = Strict`
  - `ttl = 3600000 ms`
- Reset password token dung JWT rieng:
  - `type = PASSWORD_RESET`
  - `ttl` mac dinh `900000 ms`
- Backend co the doc token tu:
  - cookie `access_token`
  - hoac header `Authorization: Bearer <token>`

## Format loi

### Loi chung cua API

Da so loi tra ve theo format:

```json
{
  "errors": [
    {
      "unifiedErrorCode": "001",
      "errorCode": "VALIDATION_ERROR",
      "errorMessage": "Invalid required fields: email Email khong dung dinh dang"
    }
  ]
}
```

### Loi security

- `401 Unauthorized`: chua dang nhap, token het han, token sai, hoac khong co token
- `403 Forbidden`: da dang nhap nhung khong du quyen

Vi du:

```json
{
  "errors": [
    {
      "unifiedErrorCode": "107",
      "errorCode": "AUTHENTICATION_REQUIRED",
      "errorMessage": "Nguoi dung chua dang nhap"
    }
  ]
}
```

### Loi rieng VNPay

`VnpayException` co the tra ve 2 kieu:

- Tai `GET /api/payment/vnpay-return`

```json
{
  "errorCode": "PAYMENT_FAILED",
  "message": "Payment failed with code: 24"
}
```

- Tai `GET /api/payment/vnpay-ipn`

```json
{
  "RspCode": "99",
  "Message": "Unknown error"
}
```

## Quy uoc content-type

- JSON:
  - phan lon endpoint `POST/PUT`
- `multipart/form-data`:
  - `POST /api/products`
  - `PUT /api/products/{id}`

## DTO response dung chung

### `UserResponse`

```json
{
  "userId": 1,
  "fullName": "Nguyen Van A",
  "email": "a@example.com",
  "phone": "0912345678",
  "address": "HCM",
  "role": "USER",
  "enabled": true,
  "createdAt": "2026-04-24T10:15:30"
}
```

Field:

- `userId`: `int`
- `fullName`: `string`
- `email`: `string`
- `phone`: `string | null`
- `address`: `string | null`
- `role`: `string`
- `enabled`: `boolean`
- `createdAt`: `LocalDateTime`

### `ProductResponse`

```json
{
  "productId": 10,
  "categoryId": 1,
  "productName": "Cay Kim Tien",
  "description": "Cay de cham",
  "careGuide": "Tuoi 2 lan/tuan",
  "price": 250000,
  "stock": 20,
  "image": "https://...",
  "active": true,
  "createdAt": "2026-04-20T08:30:00.000+00:00"
}
```

Field:

- `productId`: `Integer`
- `categoryId`: `Integer`
- `productName`: `string`
- `description`: `string | null`
- `careGuide`: `string | null`
- `price`: `double`
- `stock`: `int`
- `image`: `string | null`
- `active`: `boolean`
- `createdAt`: `Date`

### `CategoryResponse`

```json
{
  "categoryId": 1,
  "categoryName": "Cay canh",
  "description": "Nhom cay canh",
  "icon": "fa-leaf"
}
```

### `CartItemResponse`

```json
{
  "cartItemId": 5,
  "cartId": 2,
  "productId": 10,
  "quantity": 3,
  "addedAt": "2026-04-24T09:00:00.000+00:00",
  "product": {
    "productId": 10,
    "categoryId": 1,
    "productName": "Cay Kim Tien",
    "description": "Cay de cham",
    "careGuide": "Tuoi 2 lan/tuan",
    "price": 250000,
    "stock": 20,
    "image": "https://...",
    "active": true,
    "createdAt": "2026-04-20T08:30:00.000+00:00"
  }
}
```

### `CartResponse`

```json
{
  "items": [],
  "totalQuantity": 0,
  "totalAmount": 0
}
```

### `OrderItemResponse`

```json
{
  "orderItemId": 1,
  "orderId": 100,
  "productId": 10,
  "quantity": 2,
  "price": 250000
}
```

### `OrderResponse`

```json
{
  "orderId": 100,
  "userId": 1,
  "orderDate": "2026-04-24T10:30:00.000+00:00",
  "status": "Cho xu ly",
  "totalAmount": 500000,
  "shippingAddress": "123 ABC, HCM",
  "note": "Giao gio hanh chinh",
  "fullName": "Nguyen Van A",
  "items": [
    {
      "orderItemId": 1,
      "orderId": 100,
      "productId": 10,
      "quantity": 2,
      "price": 250000
    }
  ]
}
```

### `PaymentResponse`

```json
{
  "paymentId": 1,
  "orderId": 100,
  "paymentMethod": "COD",
  "amount": 500000,
  "paymentDate": "2026-04-24T10:45:00.000+00:00",
  "successful": false
}
```

## 1. Authentication

### `POST /api/auth/register`

- Auth: public
- Content-Type: `application/json`
- Request body:

```json
{
  "fullName": "Nguyen Van A",
  "email": "a@example.com",
  "password": "abc123",
  "phone": "0912345678",
  "address": "123 ABC, HCM"
}
```

- Validation:
  - `fullName`: bat buoc, max `100`
  - `email`: bat buoc, dung dinh dang email, max `150`
  - `password`: bat buoc, `6-72` ky tu, phai co it nhat `1` chu cai va `1` chu so, chi cho phep `[A-Za-z\\d@$!%*#?&]`
  - `phone`: khong bat buoc, neu co phai match `^(0|\\+84)[0-9]{8,10}$`
  - `address`: khong bat buoc, max `255`
- Business logic:
  - email bi trim
  - phone bi trim va xoa khoang trang
  - role mac dinh se la `USER`
  - `enabled = true`
  - email va phone phai unique
- Success `201 Created`:

```json
{
  "message": "Dang ky thanh cong",
  "user": {
    "userId": 1,
    "fullName": "Nguyen Van A",
    "email": "a@example.com",
    "phone": "0912345678",
    "address": "123 ABC, HCM",
    "role": "USER",
    "enabled": true,
    "createdAt": "2026-04-24T10:15:30"
  }
}
```

- Loi thuong gap:
  - `400`: validation loi
  - `400`: `EMAIL_REQUIRED`, `PASSWORD_REQUIRED`
  - `409` theo swagger, nhung trong code exception duoc map ve `400` neu di qua `BadRequestException`

### `POST /api/auth/login`

- Auth: public
- Content-Type: `application/json`
- Request body:

```json
{
  "email": "a@example.com",
  "password": "abc123"
}
```

- Validation:
  - `email`: bat buoc, email format, max `150`
  - `password`: bat buoc, cung validator password nhu register
- Success `200 OK`:

```json
{
  "message": "Dang nhap thanh cong",
  "ttl": 3600000
}
```

- Header response quan trong:
  - `Set-Cookie: access_token=<jwt>; HttpOnly; Path=/api; Domain=localhost; Secure; SameSite=Strict`
- Loi thuong gap:
  - `400`: validation loi
  - `401`: sai email/mat khau, tai khoan bi vo hieu hoa

### `POST /api/auth/forgot-password`

- Auth: public
- Content-Type: `application/json`
- Request body:

```json
{
  "email": "a@example.com",
  "newPassword": "abc123"
}
```

- Validation:
  - `email`: bat buoc, email format, max `150`
  - `newPassword`: bat buoc, cung validator password nhu register/login
- Business logic:
  - cap nhat truc tiep mat khau moi theo `email`, khong dung reset token
  - email phai ton tai va tai khoan dang `enabled`
  - `newPassword` khong duoc trung voi mat khau cu
- Success `200 OK`:

```json
{
  "message": "Dat lai mat khau thanh cong"
}
```

- Loi thuong gap:
  - `400`: validation loi
  - `404`: `USER_NOT_FOUND`

### `POST /api/auth/send-otp`

- Auth: public
- Content-Type: `application/json`
- Request body:

```json
{
  "email": "a@example.com"
}
```

- Validation:
  - `email`: bat buoc, email format, max `150`
- Business logic:
  - backend kiem tra email co ton tai trong bang `Users` truoc khi gui OTP
  - backend gui OTP 6 so qua email
  - OTP mac dinh het han sau `300` giay
  - moi lan resend phai cach lan gui truoc it nhat `120` giay
  - toi da `5` lan resend trong mot phien OTP
  - neu OTP cu da het han thi backend tao phien OTP moi va reset lai quota resend
- Success `200 OK`:

```json
{
  "message": "OTP da duoc gui den email",
  "expiresInSeconds": 300,
  "resendAvailableInSeconds": 120,
  "remainingResends": 5
}
```

- Cau hinh lien quan:
  - `app.auth.otp.expiration-seconds=300`
  - `app.auth.otp.resend-cooldown-seconds=120`
  - `app.auth.otp.max-resends=5`
- Loi thuong gap:
  - `400`: validation loi
  - `400`: `Chi duoc gui lai OTP sau <n> giay`
  - `400`: `Da vuot qua so lan gui lai OTP toi da`
  - `400`: `EMAIL_SEND_FAILED`
  - `404`: `USER_NOT_FOUND`

### `POST /api/auth/verify-otp`

- Auth: public
- Content-Type: `application/json`
- Request body:

```json
{
  "email": "a@example.com",
  "otp": "123456"
}
```

- Validation:
  - `email`: bat buoc, email format, max `150`
  - `otp`: bat buoc, dung `6` chu so
- Business logic:
  - backend tim OTP theo email
  - neu OTP het han thi xoa khoi memory va tra loi
  - neu OTP dung thi verify thanh cong va xoa OTP khoi memory
  - neu OTP sai thi tra loi
- Success `200 OK`:

```json
{
  "message": "Xac thuc OTP thanh cong"
}
```

- Loi thuong gap:
  - `400`: validation loi
  - `400`: `Khong tim thay OTP hoac OTP chua duoc gui`
  - `400`: `OTP da het han`
  - `400`: `OTP khong chinh xac`

### `POST /api/auth/reset-password`

- Auth: public
- Content-Type: `application/json`
- Request body:

```json
{
  "email": "a@example.com",
  "newPassword": "abc123"
}
```

- Validation:
  - `email`: bat buoc, email format, max `150`
  - `newPassword`: bat buoc, cung validator password nhu register/login
- Business logic:
  - cap nhat truc tiep mat khau moi theo `email`, khong dung reset token
  - email phai ton tai va tai khoan dang `enabled`
  - `newPassword` khong duoc trung voi mat khau cu
- Success `200 OK`:

```json
{
  "message": "Dat lai mat khau thanh cong"
}
```

- Loi thuong gap:
  - `400`: validation loi
  - `404`: `USER_NOT_FOUND`

### `POST /api/auth/logout`

- Auth: co the goi du da dang nhap hoac chua
- Request body: khong co
- Success `200 OK`:

```json
{
  "message": "Dang xuat thanh cong"
}
```

- Hanh vi:
  - neu tim thay token trong cookie/header thi backend se blacklist token neu token con hop le
  - du token loi/het han, backend van clear cookie

## 2. Home

## 1A. Google OAuth2

Hai endpoint duoi day khong nam trong `AuthController` nhung dang duoc Spring Security expose qua `oauth2Login()` trong `SecurityConfig`.

### `GET /oauth2/authorization/google`

- Auth: public
- Muc dich: bat dau flow dang nhap bang Google
- Hanh vi:
  - redirect nguoi dung sang trang consent cua Google
  - registration id dang dung la `google`
- Cau hinh lien quan:
  - `spring.security.oauth2.client.registration.google.client-id`
  - `spring.security.oauth2.client.registration.google.client-secret`
  - `spring.security.oauth2.client.registration.google.scope=openid,profile,email`
- Success:
  - `302 Found` redirect sang Google

### `GET /login/oauth2/code/google`

- Auth: public
- Muc dich: callback URL ma Google redirect ve sau khi dang nhap
- Cau hinh mac dinh hien tai:
  - `spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/login/oauth2/code/google`
- Success handler:
  - backend doc `email` va `name` tu `OAuth2User`
  - neu user chua ton tai thi tu tao user moi voi:
    - `role = USER`
    - `enabled = true`
    - password ngau nhien da BCrypt hash
  - backend sinh JWT cho user
- Redirect sau khi thanh cong:
  - `app.auth.oauth2.redirect-url=http://localhost:3000`
  - do `app.auth.oauth2.use-cookie-redirect=true` trong `application.properties`, backend se:
    - set cookie `access_token`
    - redirect ve frontend URL, khong gan `token` len query string
- Redirect khi that bai:
  - backend redirect ve:
    - `http://localhost:3000?error=google_login_failed`
- Success:
  - `302 Found` redirect ve frontend
- Header response quan trong:
  - `Set-Cookie: access_token=<jwt>; HttpOnly; Path=/api; Domain=localhost; SameSite=Strict`

### `GET /api/home`

- Auth: public
- Query params:
  - `plantLimit`: mac dinh `5`, min `1`
  - `potLimit`: mac dinh `5`, min `1`
  - `accessoryLimit`: mac dinh `5`, min `1`
- Success `200 OK`:

```json
{
  "cayCanh": [ProductResponse],
  "chauCay": [ProductResponse],
  "phuKien": [ProductResponse]
}
```

- Ghi chu:
  - backend hard-code category:
    - `1` = cay canh
    - `2` = chau cay
    - `3` = phu kien

## 3. Products

### `GET /api/products`

- Auth: public
- Request body: khong co
- Success `200 OK`:

```json
[
  {
    "productId": 10,
    "categoryId": 1,
    "productName": "Cay Kim Tien",
    "description": "Cay de cham",
    "careGuide": "Tuoi 2 lan/tuan",
    "price": 250000,
    "stock": 20,
    "image": "https://...",
    "active": true,
    "createdAt": "2026-04-20T08:30:00.000+00:00"
  }
]
```

### `GET /api/products/{id}`

- Auth: public
- Path param:
  - `id`: bat buoc, `> 0`
- Success `200 OK`: `ProductResponse`
- Loi:
  - `400`: id khong hop le
  - `404`: khong tim thay product

### `GET /api/products/search`

- Auth: public
- Query param:
  - `keyword`: bat buoc, khong blank, max `100`
- Business logic:
  - tim kiem theo tu khoa trong `productName` hoac `description`
  - khong phan biet chu hoa, chu thuong
- Success `200 OK`: `ProductResponse[]`
- Loi:
  - `400`: keyword rong

### `GET /api/products/category/{categoryId}`

- Auth: public
- Path param:
  - `categoryId`: bat buoc, `> 0`
- Success `200 OK`: `ProductResponse[]`
- Loi:
  - `400`: id khong hop le
  - `404`: category khong ton tai

### `POST /api/products`

- Auth: `ADMIN`
- Content-Type: `multipart/form-data`
- Form fields:
  - `categoryId`: bat buoc, `> 0`
  - `productName`: bat buoc, max `150`
  - `description`: khong bat buoc, max `5000`
  - `careGuide`: khong bat buoc, max `5000`
  - `price`: bat buoc, `> 0`, `<= 1000000000`
  - `stock`: bat buoc, `>= 0`
  - `image`: file, khong bat buoc
  - `active`: boolean
- Business logic:
  - neu co `image` thi upload Cloudinary va luu URL
  - neu khong co `image` thi `image = null`
- Success `201 Created`: `ProductResponse`

### `PUT /api/products/{id}`

- Auth: `ADMIN`
- Content-Type: `multipart/form-data`
- Path param:
  - `id`: bat buoc, `> 0`
- Form fields giong `POST /api/products`
- Business logic:
  - neu co `image` moi thi upload Cloudinary va thay image
  - neu khong gui image thi giu image cu
- Success `200 OK`: `ProductResponse`
- Loi:
  - `404`: product khong ton tai
  - `404`: category khong ton tai

### `DELETE /api/products/{id}`

- Auth: `ADMIN`
- Path param:
  - `id`: bat buoc, `> 0`
- Success `200 OK`:

```json
{
  "message": "Xoa san pham thanh cong"
}
```

## 4. Categories

### `GET /api/categories`

- Auth: public
- Success `200 OK`: `CategoryResponse[]`

### `GET /api/categories/{id}`

- Auth: public
- Path param:
  - `id`: bat buoc, `> 0`
- Success `200 OK`: `CategoryResponse`
- Loi:
  - `404`: category khong ton tai

### `POST /api/categories`

- Auth: `ADMIN`
- Content-Type: `application/json`
- Request body:

```json
{
  "categoryName": "Cay canh",
  "description": "Nhom cay canh",
  "icon": "fa-leaf"
}
```

- Validation:
  - `categoryName`: bat buoc, max `100`
  - `description`: khong bat buoc, max `500`
  - `icon`: khong bat buoc, max `100`
- Success `201 Created`: `CategoryResponse`

### `PUT /api/categories/{id}`

- Auth: `ADMIN`
- Content-Type: `application/json`
- Path param:
  - `id`: bat buoc, `> 0`
- Request body: giong create
- Success `200 OK`: `CategoryResponse`

### `DELETE /api/categories/{id}`

- Auth: `ADMIN`
- Path param:
  - `id`: bat buoc, `> 0`
- Success `200 OK`:

```json
{
  "message": "Xoa danh muc thanh cong"
}
```

## 5. Cart

Tat ca endpoint cart deu can dang nhap.

### `GET /api/cart`

- Success `200 OK`:

```json
{
  "items": [
    {
      "cartItemId": 5,
      "cartId": 2,
      "productId": 10,
      "quantity": 3,
      "addedAt": "2026-04-24T09:00:00.000+00:00",
      "product": {
        "productId": 10,
        "categoryId": 1,
        "productName": "Cay Kim Tien",
        "description": "Cay de cham",
        "careGuide": "Tuoi 2 lan/tuan",
        "price": 250000,
        "stock": 20,
        "image": "https://...",
        "active": true,
        "createdAt": "2026-04-20T08:30:00.000+00:00"
      }
    }
  ],
  "totalQuantity": 3,
  "totalAmount": 750000
}
```

- Ghi chu:
  - neu user chua co cart, backend tu tao cart rong

### `POST /api/cart/items`

- Content-Type: `application/json`
- Request body:

```json
{
  "productId": 10,
  "quantity": 2
}
```

- Validation:
  - `productId`: bat buoc, `> 0`
  - `quantity`: khong bat buoc, neu gui thi `> 0`
- Business logic:
  - neu `quantity = null` thi controller mac dinh thanh `1`
  - neu san pham da co trong cart thi backend cong don quantity
- Success `201 Created`:

```json
{
  "message": "Them san pham vao gio hang thanh cong",
  "item": CartItemResponse,
  "cart": CartResponse
}
```

- Loi:
  - `404`: product khong ton tai
  - `400`: quantity <= 0

### `PUT /api/cart/items/{cartItemId}`

- Content-Type: `application/json`
- Path param:
  - `cartItemId`: bat buoc, `> 0`
- Request body:

```json
{
  "quantity": 5
}
```

- Validation:
  - `quantity`: bat buoc, `> 0`
- Success `200 OK`:

```json
{
  "message": "Cap nhat so luong thanh cong",
  "item": CartItemResponse,
  "cart": CartResponse
}
```

- Loi:
  - `404`: cart item khong ton tai
  - `400`: cart item khong thuoc user hien tai

### `DELETE /api/cart/items/{cartItemId}`

- Path param:
  - `cartItemId`: bat buoc, `> 0`
- Success `200 OK`:

```json
{
  "message": "Xoa san pham khoi gio hang thanh cong",
  "item": null,
  "cart": CartResponse
}
```

### `DELETE /api/cart/clear`

- Success `200 OK`:

```json
{
  "message": "Da xoa toan bo gio hang"
}
```

## 6. Wishlist

Tat ca endpoint wishlist deu can dang nhap.

### `GET /api/wishlist`

- Success `200 OK`:

```json
{
  "items": [ProductResponse],
  "count": 1
}
```

### `POST /api/wishlist/{productId}`

- Path param:
  - `productId`: bat buoc, `> 0`
- Request body: khong co
- Success `201 Created`:

```json
{
  "message": "Them vao wishlist thanh cong",
  "product": ProductResponse,
  "count": 2
}
```

- Ghi chu:
  - service khong tu check duplicate trong controller; hanh vi cuoi cung phu thuoc repository/DB

### `DELETE /api/wishlist/{productId}`

- Path param:
  - `productId`: bat buoc, `> 0`
- Success `200 OK`:

```json
{
  "message": "Xoa khoi wishlist thanh cong",
  "product": null,
  "count": 1
}
```

## 7. Checkout

### `POST /api/checkout`

- Auth: can dang nhap
- Content-Type: `application/json`
- Request body:

```json
{
  "shippingAddress": "123 ABC, HCM",
  "note": "Giao gio hanh chinh"
}
```

- Validation:
  - `shippingAddress`: bat buoc, max `255`
  - `note`: khong bat buoc, max `1000`
- Business logic:
  - cart phai khong rong
  - order duoc tao voi `status = "Cho xu ly"`
  - `totalAmount` duoc tinh tu tong `quantity * product.price`
  - checkout xong se clear cart
- Success `201 Created`:

```json
{
  "message": "Dat hang thanh cong",
  "order": OrderResponse
}
```

- Loi:
  - `400`: dia chi rong
  - `400`: gio hang rong

## 8. Payment

### `POST /api/payment`

- Auth: can dang nhap
- Content-Type: `application/json`
- Request body:

```json
{
  "orderId": 100,
  "paymentMethod": "COD",
  "amount": 500000,
  "successful": false
}
```

- Validation:
  - `orderId`: bat buoc, `> 0`
  - `paymentMethod`: bat buoc, max `50`
  - `amount`: khong bat buoc, neu gui thi `> 0`
  - `successful`: khong bat buoc
- Business logic:
  - neu `amount = null` thi controller set `0`
  - neu `successful = null` thi controller set `false`
  - neu `amount <= 0` sau khi vao service thi backend tu lay `order.totalAmount`
  - chi tao payment cho order thuoc user dang login
- Success `201 Created`:

```json
{
  "message": "Tao thanh toan thanh cong",
  "payment": PaymentResponse
}
```

### `GET /api/payment/{paymentId}`

- Auth: can dang nhap
- Path param:
  - `paymentId`: bat buoc, `> 0`
- Success `200 OK`: `PaymentResponse`
- Chi tra du lieu neu payment thuoc order cua user hien tai

### `GET /api/payment/order/{orderId}`

- Auth: can dang nhap
- Path param:
  - `orderId`: bat buoc, `> 0`
- Success `200 OK`: `PaymentResponse[]`

### `POST /api/payment/create`

- Auth: public
- Content-Type: `application/json`
- Muc dich: tao URL redirect sang VNPay
- Request body:

```json
{
  "amount": 500000,
  "orderId": "100"
}
```

- Validation:
  - `amount`: bat buoc, `> 0`
  - `orderId`: bat buoc, string khong blank
- Business logic:
  - backend parse `orderId` sang `int`
  - chi cho tao link neu don hang dang o trang thai `Cho xu ly`
  - so tien gui len phai bang `order.totalAmount`
  - ip client lay tu header `X-Forwarded-For` hoac `remoteAddr`
- Success `200 OK`:

```json
{
  "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?..."
}
```

- Loi:
  - `400`: sai amount
  - `400`: order khong ton tai
  - `400`: order da duoc xu ly

### `GET /api/payment/vnpay-return`

- Auth: public
- Query params: toan bo tham so VNPay redirect ve
- Business logic:
  - co verify chu ky
  - endpoint nay chi tra ket qua hien thi, khong update DB
  - neu `vnp_ResponseCode != 00` thi nem `VnpayException`
- Success `200 OK`:

```json
{
  "orderId": "100",
  "amount": 500000,
  "transactionNo": "14725836",
  "responseCode": "00",
  "message": "Payment success, waiting for IPN confirmation",
  "success": true
}
```

### `GET /api/payment/vnpay-ipn`

- Auth: public
- Query params: toan bo tham so IPN tu VNPay
- Business logic:
  - verify chu ky
  - check amount phai khop order
  - chi order dang `Cho xu ly` moi duoc update
  - neu thanh cong:
    - `status = "Da thanh toan"`
  - neu that bai:
    - `status = "Thanh toan that bai"`
- Success `200 OK`:

```json
{
  "RspCode": "00",
  "Message": "Confirm Success"
}
```

- Loi co the tra:

```json
{
  "RspCode": "99",
  "Message": "Unknown error"
}
```

hoac:

```json
{
  "RspCode": "97",
  "Message": "Invalid signature"
}
```

Gia tri cu the phu thuoc `VnpayErrorCode`.

## 9. Orders

Tat ca endpoint orders deu can dang nhap va chi thao tac tren order cua chinh user.

### `GET /api/orders`

- Success `200 OK`: `OrderResponse[]`

### `GET /api/orders/{orderId}`

- Path param:
  - `orderId`: bat buoc, `> 0`
- Success `200 OK`: `OrderResponse`
- Loi:
  - `404`: khong tim thay order
  - `401`: order khong thuoc user hien tai

### `GET /api/orders/{orderId}/items`

- Path param:
  - `orderId`: bat buoc, `> 0`
- Success `200 OK`:

```json
[
  {
    "orderItemId": 1,
    "orderId": 100,
    "productId": 10,
    "quantity": 2,
    "price": 250000
  }
]
```

## 10. User

### `GET /api/user`

- Auth: `ADMIN`
- Dieu kien route: request khong co query param `email`
- Success `200 OK`: `UserResponse[]`

### `GET /api/user?email=...`

- Auth: `ADMIN`
- Query param:
  - `email`: bat buoc, khong blank, dung email format, max `150`
- Route nay khop ca:
  - `/api/user?email=a@example.com`
  - `/api/user/email?email=a@example.com`
- Success `200 OK`: `UserResponse`
- Ghi chu:
  - service co `trim()` email truoc khi query

### `GET /api/user/me`

- Auth: can dang nhap
- Success `200 OK`: `UserResponse`

### `PUT /api/user/me/password`

- Auth: can dang nhap
- Content-Type: `application/json`
- Request body:

```json
{
  "oldPassword": "abc123",
  "newPassword": "newpass123"
}
```

- Validation:
  - `oldPassword`: bat buoc
  - `newPassword`: bat buoc, `6-72` ky tu, phai co it nhat `1` chu cai va `1` chu so, chi cho phep `[A-Za-z\\d@$!%*#?&]`
- Business logic:
  - xac thuc user hien tai tu JWT
  - `oldPassword` phai khop mat khau dang luu
  - `newPassword` khong duoc trung voi mat khau cu
  - `newPassword` duoc ma hoa truoc khi update
- Success `200 OK`:

```json
{
  "message": "Doi mat khau thanh cong"
}
```

- Loi:
  - `400`: validation loi
  - `400`: `CURRENT_PASSWORD_INCORRECT`
  - `401`: chua dang nhap hoac token khong hop le

## 11. Order History

### `GET /api/user/orders`

- Auth: can dang nhap
- Success `200 OK`:

```json
{
  "orders": [OrderResponse],
  "totalOrders": 3
}
```

## 12. Admin

Tat ca endpoint duoi day deu can role `ADMIN`.

### `GET /api/admin/dashboard`

- Success `200 OK`:

```json
{
  "totalOrders": 120,
  "totalRevenue": 45000000,
  "totalCustomers": 38
}
```

### `GET /api/admin/orders`

- Success `200 OK`: `OrderResponse[]`

### `PUT /api/admin/orders/{id}/status`

- Content-Type: `application/json`
- Path param:
  - `id`: bat buoc, `> 0`
- Request body:

```json
{
  "status": "Dang giao"
}
```

- Validation:
  - `status`: bat buoc, max `50`
- Success `200 OK`: `OrderResponse`

### `DELETE /api/admin/orders/{id}`

- Path param:
  - `id`: bat buoc, `> 0`
- Success `200 OK`:

```json
{
  "message": "Xoa order thanh cong"
}
```

### `PUT /api/admin/users/{id}/password`

- Content-Type: `application/json`
- Path param:
  - `id`: bat buoc, `> 0`
- Request body:

```json
{
  "password": "abc123"
}
```

- Validation:
  - `password`: bat buoc, dung validator password nhu auth register/login
- Business logic:
  - password duoc hash bang BCrypt truoc khi luu
- Success `200 OK`:

```json
{
  "message": "Update password thanh cong"
}
```

### `DELETE /api/admin/users/{id}`

- Path param:
  - `id`: bat buoc, `> 0`
- Success `200 OK`:

```json
{
  "message": "Xoa user thanh cong"
}
```

### `POST /api/admin/register`

- Content-Type: `application/json`
- Request body:

```json
{
  "fullName": "Admin B",
  "email": "adminb@example.com",
  "password": "abc123",
  "phone": "0912345678",
  "address": "Ha Noi",
  "role": "ADMIN"
}
```

- Validation:
  - `fullName`: bat buoc, max `100`
  - `email`: bat buoc, email format, max `150`
  - `password`: bat buoc, validator password
  - `phone`: khong bat buoc, validator phone
  - `address`: khong bat buoc, max `255`
  - `role`: bat buoc, chi nhan `ADMIN` hoac `USER`
- Success `201 Created`:

```json
{
  "message": "Tao tai khoan thanh cong",
  "user": UserResponse
}
```

## Bang tong hop nhanh

| Module | Endpoint count |
|---|---:|
| Auth | 9 |
| Home | 1 |
| Products | 6 |
| Categories | 5 |
| Cart | 5 |
| Wishlist | 3 |
| Checkout | 1 |
| Payment | 5 |
| Orders | 3 |
| User | 3 |
| Order History | 1 |
| Admin | 7 |
| Tong | 49 |

## Ghi chu quan trong

- `POST/PUT /api/products` dung `@ModelAttribute`, khong phai JSON.
- `logout` khong can request body.
- Quen mat khau hien gui email qua Gmail SMTP; de chay thuc te can dung Google App Password, khong nen dung mat khau Gmail thuong.
- `amount` trong `POST /api/payment/create` la so tien don vi chinh, service tu nhan `long` roi nhan `100` de doi sang minor unit VNPay.
- `GET /api/payment/vnpay-return` khong update trang thai order; trang thai order duoc update o `GET /api/payment/vnpay-ipn`.
- Cac field `Date`/`LocalDateTime` se serialize theo Jackson config thuc te cua app; vi du tren day la minh hoa.
