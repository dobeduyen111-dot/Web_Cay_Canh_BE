# Backend CayCanh

Backend cho hệ thống bán cây cảnh, viết bằng `Java 21` và `Spring Boot 3.5.7`.

## Công nghệ chính

- `Java 21`
- `Spring Boot`
- `Spring Security`
- `JWT`
- `Spring JDBC`
- `PostgreSQL`
- `Swagger / OpenAPI`
- `Cloudinary`
- `VNPAY`
- `OAuth2 Google`
- `Maven Wrapper`

## Cấu trúc chính

```text
src/main/java/ceb
├── config
├── controller
├── domain
├── exception
├── repository
├── security
├── service
├── util
└── validation
```

## Yêu cầu môi trường

Trước khi chạy bằng VS Code, máy cần có:

- `JDK 21`
- `VS Code`
- `PostgreSQL`

Nên cài thêm các extension trong VS Code:

- `Extension Pack for Java`
- `Spring Boot Extension Pack`

## Mở project trong VS Code

1. Mở thư mục `backend`.
2. Chờ VS Code tải Maven dependencies.
3. Kiểm tra máy đang dùng đúng `Java 21`.
4. Mở terminal trong VS Code tại thư mục `backend`.

## Cấu hình database

Project đang đọc cấu hình từ file `src/main/resources/application.properties`.

Cấu hình hiện tại:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/caycanhdb
spring.datasource.username=root
spring.datasource.password=root
```

Bạn cần:

1. Tạo database `caycanhdb` trong PostgreSQL.
2. Tạo user `root` với password `root`.
3. Nếu username/password/port khác, sửa lại `application.properties`.

Ví dụ tạo database và user:

```sql
CREATE USER root WITH PASSWORD 'root';
CREATE DATABASE caycanhdb OWNER root;
GRANT ALL PRIVILEGES ON DATABASE caycanhdb TO root;
```

## Lưu ý về port PostgreSQL

Project mặc định kết nối PostgreSQL tại **port 5433**. Tuy nhiên PostgreSQL khi mới cài thường chạy ở **port 5432**.

Kiểm tra port PostgreSQL đang chạy:

```bash
sudo -u postgres psql -c "SHOW port;"
```

**Nếu kết quả là 5432** (không khớp với project), có 2 cách xử lý:

### Cách 1: Đổi port PostgreSQL sang 5433 (khuyến nghị, giữ nguyên config project)

```bash
sudo nano /etc/postgresql/*/main/postgresql.conf
```

Tìm dòng `port = 5432`, sửa thành:

```
port = 5433
```

Lưu file rồi restart PostgreSQL:

```bash
sudo systemctl restart postgresql
```

### Cách 2: Sửa port trong application.properties sang 5432

Mở file `src/main/resources/application.properties`, sửa dòng:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/caycanhdb
```

## Cấu hình application.properties

File `src/main/resources/application.properties` chứa nhiều giá trị nhạy cảm đã bị ẩn. Bạn cần tự điền các giá trị sau trước khi chạy project.

### JWT

```properties
app.jwt.secret=<chuỗi bí mật tự đặt, ít nhất 32 ký tự>
app.jwt.expiration-ms=<thời gian hết hạn token, tính bằng milliseconds, ví dụ: 3600000>
```

### Google OAuth2

Tạo OAuth2 credentials tại [Google Cloud Console](https://console.cloud.google.com/).

```properties
spring.security.oauth2.client.registration.google.client-id=<Google Client ID>
spring.security.oauth2.client.registration.google.client-secret=<Google Client Secret>
```

### Cloudinary

Đăng ký tài khoản tại [cloudinary.com](https://cloudinary.com/) để lấy thông tin.

```properties
cloudinary.cloud-name=<cloud name>
cloudinary.api-key=<api key>
cloudinary.api-secret=<api secret>
```

### VNPAY

Đăng ký tài khoản sandbox tại [sandbox.vnpayment.vn](https://sandbox.vnpayment.vn/).

```properties
vnpay.vnp_TmnCode=<TMN Code>
vnpay.vnp_HashSecret=<Hash Secret>
vnpay.vnp_PayUrl=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.vnp_ReturnUrl=http://localhost:3000/payment/vnpay-return
```

### Mail (Gmail)

Bật "App Password" trong tài khoản Google để lấy mật khẩu ứng dụng.

```properties
spring.mail.username=<địa chỉ Gmail>
spring.mail.password=<App Password của Gmail>
```

> ⚠️ **Lưu ý:** Không commit file `application.properties` có chứa thông tin thật lên GitHub.

## Chạy project trong VS Code

### Cách 1: chạy bằng terminal

```bash
./mvnw spring-boot:run
```

### Cách 2: chạy bằng nút Run của VS Code

- Mở file `src/main/java/ceb/CayCanhApplication.java`
- Bấm `Run` tại hàm `main`

Khi chạy thành công, backend mặc định dùng:

- Base URL: `http://localhost:8080`

Để kiểm tra backend đang chạy, mở trình duyệt và truy cập:

```
http://localhost:8080/swagger-ui.html
```

Nếu Swagger UI hiện ra thì backend đã hoạt động bình thường.

## Xử lý lỗi thường gặp

### Lỗi: `maven-wrapper.properties: No such file`

File bị đặt tên sai thành `.txt`. Chạy lệnh sau để đổi tên:

```bash
mv .mvn/wrapper/maven-wrapper.properties.txt .mvn/wrapper/maven-wrapper.properties
```

### Lỗi: `Connection to localhost:5433 refused`

PostgreSQL chưa chạy hoặc chạy sai port. Kiểm tra và khởi động:

```bash
sudo systemctl start postgresql
sudo systemctl status postgresql
```

### Lỗi: `password authentication failed for user "root"`

User `root` chưa tồn tại trong PostgreSQL. Tạo user và database:

```bash
sudo -u postgres psql
```

```sql
CREATE USER root WITH PASSWORD 'root';
CREATE DATABASE caycanhdb OWNER root;
GRANT ALL PRIVILEGES ON DATABASE caycanhdb TO root;
\q
```

## Build project

```bash
./mvnw clean package
```

File build tạo ra nằm trong thư mục `target/`.

## Tài liệu API

Sau khi chạy app, có thể mở:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Nhóm API chính

- `auth`
- `products`
- `categories`
- `cart`
- `checkout`
- `orders`
- `payment`
- `wishlist`
- `admin`
- `user`
- `home`

Phần lớn API nằm dưới prefix `/api`.

## Chạy test

```bash
./mvnw test
```

## Một số file quan trọng

- `pom.xml`
- `src/main/resources/application.properties`
- `src/main/java/ceb/CayCanhApplication.java`
- `src/main/java/ceb/config/SecurityConfig.java`
- `src/main/java/ceb/config/VnpayConfig.java`
- `src/main/java/ceb/config/CloudinaryConfig.java`


- biến môi trường
- `application-local.properties`
- hoặc file cấu hình riêng không commit lên git
