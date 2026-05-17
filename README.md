Backend CayCanh
Backend cho hệ thống bán cây cảnh, viết bằng Java 21 và Spring Boot 3.5.7.
Công nghệ chính

Java 21
Spring Boot
Spring Security
JWT
Spring JDBC
PostgreSQL
Swagger / OpenAPI
Cloudinary
VNPAY
OAuth2 Google
Maven Wrapper

Cấu trúc chính
textsrc/main/java/ceb
├── config
├── controller
├── domain
├── exception
├── repository
├── security
├── service
├── util
└── validation
Yêu cầu môi trường
Trước khi chạy bằng VS Code, máy cần có:

JDK 21
VS Code
PostgreSQL

Nên cài thêm các extension trong VS Code:

Extension Pack for Java
Spring Boot Extension Pack

Mở project trong VS Code

Mở thư mục backend.
Chờ VS Code tải Maven dependencies.
Kiểm tra máy đang dùng đúng Java 21.
Mở terminal trong VS Code tại thư mục backend.

Cấu hình database
Project đang đọc cấu hình từ file src/main/resources/application.properties.
Cấu hình hiện tại:
propertiesspring.datasource.url=jdbc:postgresql://localhost:5432/caycanhdb
spring.datasource.username=postgres
spring.datasource.password=123
Bạn cần:

Tạo database caycanhdb trong PostgreSQL.
Nếu username/password khác, sửa lại application.properties.

Ví dụ tạo database:
sqlCREATE DATABASE caycanhdb;
Cấu hình application.properties
File src/main/resources/application.properties chứa nhiều giá trị nhạy cảm đã bị ẩn. Bạn cần tự điền các giá trị sau trước khi chạy project.
JWT
propertiesjwt.secret=<chuỗi bí mật tự đặt, ít nhất 32 ký tự>
jwt.expiration=<thời gian hết hạn token, tính bằng milliseconds, ví dụ: 86400000>
Google OAuth2
Tạo OAuth2 credentials tại Google Cloud Console.
propertiesspring.security.oauth2.client.registration.google.client-id=<Google Client ID>
spring.security.oauth2.client.registration.google.client-secret=<Google Client Secret>
Cloudinary
Đăng ký tài khoản tại cloudinary.com để lấy thông tin.
propertiescloudinary.cloud-name=<cloud name>
cloudinary.api-key=<api key>
cloudinary.api-secret=<api secret>
VNPAY
Đăng ký tài khoản sandbox tại sandbox.vnpayment.vn.
propertiesvnpay.tmn-code=<TMN Code>
vnpay.hash-secret=<Hash Secret>
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=http://localhost:8080/api/payment/vnpay-return
Mail (Gmail)
Bật "App Password" trong tài khoản Google để lấy mật khẩu ứng dụng.
propertiesspring.mail.username=<địa chỉ Gmail>
spring.mail.password=<App Password của Gmail>

⚠️ Lưu ý: Không commit file application.properties có chứa thông tin thật lên GitHub.

Chạy project trong VS Code
Cách 1: chạy bằng terminal
bash./mvnw spring-boot:run
Cách 2: chạy bằng nút Run của VS Code

Mở file src/main/java/ceb/CayCanhApplication.java
Bấm Run tại hàm main

Khi chạy thành công, backend mặc định dùng:

Base URL: http://localhost:8080

Để kiểm tra backend đang chạy, mở trình duyệt và truy cập:
http://localhost:8080/swagger-ui.html
Nếu Swagger UI hiện ra thì backend đã hoạt động bình thường.
Build project
bash./mvnw clean package
File build tạo ra nằm trong thư mục target/.
Tài liệu API
Sau khi chạy app, có thể mở:

Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs

Nhóm API chính

auth
products
categories
cart
checkout
orders
payment
wishlist
admin
user
home

Phần lớn API nằm dưới prefix /api.
Chạy test
bash./mvnw test
Một số file quan trọng

pom.xml
src/main/resources/application.properties
src/main/java/ceb/CayCanhApplication.java
src/main/java/ceb/config/SecurityConfig.java
src/main/java/ceb/config/VnpayConfig.java
src/main/java/ceb/config/CloudinaryConfig.java

Lưu ý kỹ thuật

Project đang dùng Maven Wrapper, nên không bắt buộc cài Maven global.
packaging trong pom.xml là war, nhưng vẫn có thể chạy local bình thường bằng spring-boot:run.
Repository hiện tại nghiêng về Spring JDBC và truy vấn SQL thủ công.
File application.properties đang chứa nhiều khóa nhạy cảm như JWT, mail, Google OAuth, Cloudinary, VNPAY.
