package ceb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ceb.domain.model.CartItem;
import ceb.domain.req.CartAddItemRequest;
import ceb.domain.req.CartUpdateItemRequest;
import ceb.domain.res.CartItemResponse;
import ceb.domain.res.CartOperationResponse;
import ceb.domain.res.CartResponse;
import ceb.domain.res.MessageResponse;
import ceb.service.service.CartService;
import ceb.service.service.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@Validated
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;

    public CartController(CartService cartService, CurrentUserService currentUserService) {
        this.cartService = cartService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public CartResponse view(Authentication authentication) {
        int userId = currentUserService.getCurrentUserId(authentication);
        return buildCartResponse(userId);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartOperationResponse addItem(
            Authentication authentication,
            @Valid @RequestBody CartAddItemRequest request) {
        int userId = currentUserService.getCurrentUserId(authentication);
        int quantity = request.getQuantity() == null ? 1 : request.getQuantity();
        CartItem cartItem = cartService.addItem(userId, request.getProductId(), quantity);
        return new CartOperationResponse(
                "Them san pham vao gio hang thanh cong",
                CartItemResponse.from(cartItem),
                buildCartResponse(userId));
    }

    @PutMapping("/items/{cartItemId}")
    public CartOperationResponse updateQuantity(
            Authentication authentication,
            @PathVariable @Positive(message = "Cart item id phai lon hon 0") int cartItemId,
            @Valid @RequestBody CartUpdateItemRequest request) {
        int userId = currentUserService.getCurrentUserId(authentication);
        CartItem cartItem = cartService.updateQuantity(userId, cartItemId, request.getQuantity());
        return new CartOperationResponse(
                "Cap nhat so luong thanh cong",
                CartItemResponse.from(cartItem),
                buildCartResponse(userId));
    }

    @DeleteMapping("/items/{cartItemId}")
    public CartOperationResponse removeItem(
            Authentication authentication,
            @PathVariable @Positive(message = "Cart item id phai lon hon 0") int cartItemId) {
        int userId = currentUserService.getCurrentUserId(authentication);
        cartService.removeItem(userId, cartItemId);
        return new CartOperationResponse("Xoa san pham khoi gio hang thanh cong", null, buildCartResponse(userId));
    }

    @DeleteMapping("/clear")
    public MessageResponse clear(Authentication authentication) {
        int userId = currentUserService.getCurrentUserId(authentication);
        cartService.clear(userId);
        return new MessageResponse("Da xoa toan bo gio hang");
    }

    private CartResponse buildCartResponse(int userId) {
        return new CartResponse(
                cartService.getItems(userId).stream().map(CartItemResponse::from).toList(),
                cartService.getTotalQuantity(userId),
                cartService.getTotalAmount(userId));
    }
}
