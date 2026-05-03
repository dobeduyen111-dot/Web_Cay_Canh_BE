package ceb.service.service;

import java.util.List;

import ceb.domain.model.Products;

public interface WishlistService {

    List<Products> getWishlist(int userId);

    Products add(int userId, int productId);

    void remove(int userId, int productId);

    int count(int userId);
}
