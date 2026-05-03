package ceb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ceb.domain.model.Products;
import ceb.domain.res.ProductResponse;
import ceb.domain.res.WishlistOperationResponse;
import ceb.domain.res.WishlistResponse;
import ceb.service.service.CurrentUserService;
import ceb.service.service.WishlistService;
import jakarta.validation.constraints.Positive;

@Validated
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final CurrentUserService currentUserService;

    public WishlistController(WishlistService wishlistService, CurrentUserService currentUserService) {
        this.wishlistService = wishlistService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public WishlistResponse getWishlist(Authentication authentication) {
        int userId = currentUserService.getCurrentUserId(authentication);
        return new WishlistResponse(
                wishlistService.getWishlist(userId).stream().map(ProductResponse::from).toList(),
                wishlistService.count(userId));
    }

    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistOperationResponse add(
            Authentication authentication,
            @PathVariable @Positive(message = "Product id phai lon hon 0") int productId) {
        int userId = currentUserService.getCurrentUserId(authentication);
        Products product = wishlistService.add(userId, productId);
        return new WishlistOperationResponse(
                "Them vao wishlist thanh cong",
                ProductResponse.from(product),
                wishlistService.count(userId));
    }

    @DeleteMapping("/{productId}")
    public WishlistOperationResponse remove(
            Authentication authentication,
            @PathVariable @Positive(message = "Product id phai lon hon 0") int productId) {
        int userId = currentUserService.getCurrentUserId(authentication);
        wishlistService.remove(userId, productId);
        return new WishlistOperationResponse("Xoa khoi wishlist thanh cong", null, wishlistService.count(userId));
    }
}
