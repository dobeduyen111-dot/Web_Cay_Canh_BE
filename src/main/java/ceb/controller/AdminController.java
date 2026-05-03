package ceb.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
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

import ceb.domain.entity.Users;
import ceb.domain.req.AdminCreateUserRequest;
import ceb.domain.req.AdminUpdateOrderStatusRequest;
import ceb.domain.req.AdminUpdatePasswordRequest;
import ceb.domain.res.AuthRegisterResponse;
import ceb.domain.res.DashboardResponse;
import ceb.domain.res.MessageResponse;
import ceb.domain.res.OrderResponse;
import ceb.domain.res.UserResponse;
import ceb.service.service.AuthService;
import ceb.service.service.DashboardService;
import ceb.service.service.OrderService;
import ceb.service.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@Validated
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsersService usersService;
    private final OrderService orderService;
    private final DashboardService dashboardService;
    private final AuthService authService;

    public AdminController(UsersService usersService, OrderService orderService, DashboardService dashboardService, AuthService authService) {
        this.usersService = usersService;
        this.orderService = orderService;
        this.dashboardService = dashboardService;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        return dashboardService.getDashboard();
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

    @DeleteMapping("/users/{id}")
    public MessageResponse deleteUser(
            @PathVariable @Positive(message = "User id phai lon hon 0") int id) {
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
}
