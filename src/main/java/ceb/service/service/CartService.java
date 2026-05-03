package ceb.service.service;

import java.util.List;

import ceb.domain.model.CartItem;

public interface CartService {

    List<CartItem> getItems(int userId);

    CartItem addItem(int userId, int productId, int quantity);

    CartItem updateQuantity(int userId, int cartItemId, int quantity);

    void removeItem(int userId, int cartItemId);

    void clear(int userId);

    int getTotalQuantity(int userId);

    double getTotalAmount(int userId);
}
