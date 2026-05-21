package ceb.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;

import ceb.domain.entity.Users;
import ceb.domain.req.AdminCreateUserRequest;
import ceb.domain.req.AdminUpdateOrderStatusRequest;
import ceb.domain.req.AdminUpdatePasswordRequest;
import ceb.domain.req.AdminUpdateUserRoleRequest;
import ceb.domain.req.AdminUpdateUserStatusRequest;
import ceb.domain.res.AdminCustomerResponse;
import ceb.domain.res.AdminSearchResponse;
import ceb.domain.res.AuthRegisterResponse;
import ceb.domain.res.DashboardResponse;
import ceb.domain.res.MessageResponse;
import ceb.domain.res.OrderResponse;
import ceb.domain.res.PagedResponse;
import ceb.domain.res.ProductResponse;
import ceb.domain.res.UserResponse;
import ceb.exception.BadRequestException;
import ceb.service.implement.AdminSearchService;
import ceb.service.service.AuthService;
import ceb.service.service.CurrentUserService;
import ceb.service.service.DashboardService;
import ceb.service.service.OrderService;
import ceb.service.service.ProductsService;
import ceb.service.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsersService usersService;
    private final OrderService orderService;
    private final DashboardService dashboardService;
    private final AuthService authService;
    private final ProductsService productsService;
    private final CurrentUserService currentUserService;
    private final AdminSearchService adminSearchService;

    public AdminController(
            UsersService usersService,
            OrderService orderService,
            DashboardService dashboardService,
            AuthService authService,
            ProductsService productsService,
            CurrentUserService currentUserService,
            AdminSearchService adminSearchService) {
        this.usersService = usersService;
        this.orderService = orderService;
        this.dashboardService = dashboardService;
        this.authService = authService;
        this.productsService = productsService;
        this.currentUserService = currentUserService;
        this.adminSearchService = adminSearchService;
    }

    public DashboardResponse getDashboard() {
        return dashboardService.getDashboard();
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "5") @Min(value = 1, message = "recentLimit phai lon hon hoac bang 1") @Max(value = 20, message = "recentLimit phai nho hon hoac bang 20") int recentLimit) {
        return dashboardService.getDashboard(fromDate, toDate, recentLimit);
    }

    @GetMapping("/orders")
    public List<OrderResponse> getAllOrders() {
        return orderService.findAll().stream().map(OrderResponse::from).toList();
    }

    @PutMapping("/orders/{id}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable @Positive(message = "Order id phai lon hon 0") int id,
            @Valid @RequestBody AdminUpdateOrderStatusRequest request) {
        return OrderResponse.from(orderService.updateStatus(id, request.getStatus()));
    }

    @PutMapping("/users/{id}/password")
    public MessageResponse updateUserPassword(
            @PathVariable @Positive(message = "User id phai lon hon 0") int id,
            @Valid @RequestBody AdminUpdatePasswordRequest request) {
        usersService.updatePassword(id, request.getPassword());
        return new MessageResponse("Update password thanh cong");
    }

    @PutMapping("/users/{id}/role")
    public UserResponse updateUserRole(
            Authentication authentication,
            @PathVariable @Positive(message = "User id phai lon hon 0") int id,
            @Valid @RequestBody AdminUpdateUserRoleRequest request) {
        return UserResponse.from(usersService.updateRole(authentication, id, request.getRole()));
    }

    @PutMapping("/users/{id}/enabled")
    public UserResponse updateUserEnabled(
            Authentication authentication,
            @PathVariable @Positive(message = "User id phai lon hon 0") int id,
            @Valid @RequestBody AdminUpdateUserStatusRequest request) {
        return UserResponse.from(usersService.updateEnabled(authentication, id, request.isEnabled()));
    }

    @DeleteMapping("/users/{id}")
    public MessageResponse deleteUser(
            Authentication authentication,
            @PathVariable @Positive(message = "User id phai lon hon 0") int id) {
        if (currentUserService.getCurrentUserId(authentication) == id) {
            throw new BadRequestException("Khong the xoa tai khoan admin dang dang nhap");
        }
        usersService.deleteById(id);
        return new MessageResponse("Xoa user thanh cong");
    }

    public MessageResponse deleteUser(int id) {
        usersService.deleteById(id);
        return new MessageResponse("Xoa user thanh cong");
    }

    @DeleteMapping("/orders/{id}")
    public MessageResponse deleteOrder(
            @PathVariable @Positive(message = "Order id phai lon hon 0") int id) {
        orderService.delete(id);
        return new MessageResponse("Xoa order thanh cong");
    }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Admin tạo tài khoản mới", description = "Admin tạo tài khoản mới cho admin khác hoặc user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tạo tài khoản thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
        @ApiResponse(responseCode = "409", description = "Email đã tồn tại")
    })
    public AuthRegisterResponse createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        Users user = new Users();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(request.getRole());

        Users createdUser = authService.register(user);
        return new AuthRegisterResponse("Tao tai khoan thanh cong", UserResponse.from(createdUser));
    }

    @GetMapping("/products")
    public PagedResponse<ProductResponse> getAdminProducts(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page phai lon hon hoac bang 1") int page,
            @RequestParam(defaultValue = "8") @Min(value = 1, message = "size phai lon hon hoac bang 1") @Max(value = 100, message = "size phai nho hon hoac bang 100") int size,
            @RequestParam(required = false) @Size(max = 100, message = "keyword khong duoc vuot qua 100 ky tu") String keyword) {
        return productsService.findAdminPage(page, size, keyword);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getAdminProduct(
            @PathVariable @Positive(message = "Product id phai lon hon 0") int id) {
        return ProductResponse.from(productsService.findById(id));
    }

    @GetMapping("/customers")
    public PagedResponse<AdminCustomerResponse> getAdminCustomers(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page phai lon hon hoac bang 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size phai lon hon hoac bang 1") @Max(value = 100, message = "size phai nho hon hoac bang 100") int size,
            @RequestParam(required = false) @Size(max = 100, message = "keyword khong duoc vuot qua 100 ky tu") String keyword) {
        return usersService.findAdminCustomersPage(page, size, keyword);
    }

    @GetMapping("/search")
    public AdminSearchResponse quickSearch(
            @RequestParam @Size(min = 1, max = 100, message = "q phai tu 1 den 100 ky tu") String q) {
        return adminSearchService.search(q);
    }
}
