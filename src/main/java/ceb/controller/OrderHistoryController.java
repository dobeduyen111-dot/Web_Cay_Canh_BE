package ceb.controller;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ceb.domain.res.OrderHistoryResponse;
import ceb.domain.res.OrderResponse;
import ceb.service.service.CurrentUserService;
import ceb.service.service.OrderService;

@Validated
@RestController
@RequestMapping("/api/user/orders")
public class OrderHistoryController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    public OrderHistoryController(OrderService orderService, CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public OrderHistoryResponse history(Authentication authentication) {
        int userId = currentUserService.getCurrentUserId(authentication);
        var orders = orderService.findByUserId(userId).stream().map(OrderResponse::from).toList();
        return new OrderHistoryResponse(orders, orders.size());
    }
}
