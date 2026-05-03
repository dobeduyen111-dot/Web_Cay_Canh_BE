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

Project đang đọc cấu hình từ file [src/main/resources/application.properties](/home/pyndyn/CayCanhMNM/caycanhmnm-20260429T160227Z-3-001/caycanhmnm/backend/src/main/resources/application.properties:1).

Cấu hình hiện tại:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/caycanhdb
spring.datasource.username=postgres
spring.datasource.password=123
```

Bạn cần:

1. Tạo database `caycanhdb` trong PostgreSQL.
2. Nếu username/password khác, sửa lại `application.properties`.

Ví dụ tạo database:

```sql
CREATE DATABASE caycanhdb;
```

## Chạy project trong VS Code

### Cách 1: chạy bằng terminal

```bash
./mvnw spring-boot:run
```

### Cách 2: chạy bằng nút Run của VS Code

- Mở file [src/main/java/ceb/CayCanhApplication.java](/home/pyndyn/CayCanhMNM/caycanhmnm-20260429T160227Z-3-001/caycanhmnm/backend/src/main/java/ceb/CayCanhApplication.java:1)
- Bấm `Run` tại hàm `main`

Khi chạy thành công, backend mặc định dùng:

- Base URL: `http://localhost:8080`

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

- [pom.xml](/home/pyndyn/CayCanhMNM/caycanhmnm-20260429T160227Z-3-001/caycanhmnm/backend/pom.xml:1)
- [src/main/resources/application.properties](/home/pyndyn/CayCanhMNM/caycanhmnm-20260429T160227Z-3-001/caycanhmnm/backend/src/main/resources/application.properties:1)
- [src/main/java/ceb/CayCanhApplication.java](/home/pyndyn/CayCanhMNM/caycanhmnm-20260429T160227Z-3-001/caycanhmnm/backend/src/main/java/ceb/CayCanhApplication.java:1)
- [src/main/java/ceb/config/SecurityConfig.java](/home/pyndyn/CayCanhMNM/caycanhmnm-20260429T160227Z-3-001/caycanhmnm/backend/src/main/java/ceb/config/SecurityConfig.java:1)
- [src/main/java/ceb/config/VnpayConfig.java](/home/pyndyn/CayCanhMNM/caycanhmnm-20260429T160227Z-3-001/caycanhmnm/backend/src/main/java/ceb/config/VnpayConfig.java:1)
- [src/main/java/ceb/config/CloudinaryConfig.java](/home/pyndyn/CayCanhMNM/caycanhmnm-20260429T160227Z-3-001/caycanhmnm/backend/src/main/java/ceb/config/CloudinaryConfig.java:1)

## Lưu ý kỹ thuật

- Project đang dùng `Maven Wrapper`, nên không bắt buộc cài Maven global.
- `packaging` trong `pom.xml` là `war`, nhưng vẫn có thể chạy local bình thường bằng `spring-boot:run`.
- Repository hiện tại nghiêng về `Spring JDBC` và truy vấn SQL thủ công.
- File `application.properties` đang chứa nhiều khóa nhạy cảm như JWT, mail, Google OAuth, Cloudinary, VNPAY.

## Khuyến nghị

Không nên giữ secrets thật trong repo. Nếu tiếp tục phát triển, nên tách các giá trị nhạy cảm sang:

- biến môi trường
- `application-local.properties`
- hoặc file cấu hình riêng không commit lên git
