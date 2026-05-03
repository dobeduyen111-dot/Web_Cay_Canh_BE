package ceb.domain.res;

import java.util.Date;

import ceb.domain.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private int cartItemId;
    private int cartId;
    private int productId;
    private int quantity;
    private Date addedAt;
    private ProductResponse product;

    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(
                item.getCartItemId(),
                item.getCartId(),
                item.getProductId(),
                item.getQuantity(),
                item.getAddedAt(),
                item.getProduct() == null ? null : ProductResponse.from(item.getProduct()));
    }
}
