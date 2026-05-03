package ceb.domain.model;

import java.util.Date;

public class Wishlist {
    private Integer WishlistId;
    private Integer UserId;
    private Integer ProductId;
    private Date AddedAt;

    public Wishlist() {}

    public Wishlist(Integer wishlistId, Integer userId, Integer productId, Date addedAt) {
        WishlistId = wishlistId;
        UserId = userId;
        ProductId = productId;
        AddedAt = addedAt;
    }

    public Integer getWishlistId() { return WishlistId; }
    public void setWishlistId(Integer wishlistId) { WishlistId = wishlistId; }

    public Integer getUserId() { return UserId; }
    public void setUserId(Integer userId) { UserId = userId; }

    public Integer getProductId() { return ProductId; }
    public void setProductId(Integer productId) { ProductId = productId; }

    public Date getAddedAt() { return AddedAt; }
    public void setAddedAt(Date addedAt) { AddedAt = addedAt; }
}
