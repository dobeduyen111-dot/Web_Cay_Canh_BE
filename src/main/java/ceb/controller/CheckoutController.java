package ceb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ceb.domain.model.Orders;
import ceb.domain.req.CheckoutRequest;
import ceb.domain.res.CheckoutResponse;
import ceb.domain.res.OrderResponse;
import ceb.service.service.CurrentUserService;
import ceb.service.service.OrderService;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    public CheckoutController(OrderService orderService, CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse checkout(
            Authentication authentication,
            @Valid @RequestBody CheckoutRequest request) {
        int userId = currentUserService.getCurrentUserId(authentication);
        Orders order = orderService.checkout(userId, request.getShippingAddress(), request.getNote());
        return new CheckoutResponse("Dat hang thanh cong", OrderResponse.from(order));
    }
}
