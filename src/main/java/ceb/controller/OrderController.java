package ceb.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ceb.domain.res.OrderItemResponse;
import ceb.domain.res.OrderResponse;
import ceb.service.service.CurrentUserService;
import ceb.service.service.OrderService;
import jakarta.validation.constraints.Positive;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    public OrderController(OrderService orderService, CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<OrderResponse> getMyOrders(Authentication authentication) {
        int userId = currentUserService.getCurrentUserId(authentication);
        return orderService.findByUserId(userId).stream().map(OrderResponse::from).toList();
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            Authentication authentication,
            @PathVariable @Positive(message = "Order id phai lon hon 0") int orderId) {
        int userId = currentUserService.getCurrentUserId(authentication);
        return OrderResponse.from(orderService.findByIdForUser(userId, orderId));
    }

    @GetMapping("/{orderId}/items")
    public List<OrderItemResponse> getOrderItems(
            Authentication authentication,
            @PathVariable @Positive(message = "Order id phai lon hon 0") int orderId) {
        int userId = currentUserService.getCurrentUserId(authentication);
        return orderService.findItemsByOrderIdForUser(userId, orderId).stream().map(OrderItemResponse::from).toList();
    }
}
