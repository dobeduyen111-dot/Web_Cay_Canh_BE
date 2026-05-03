package ceb.domain.res;

import ceb.domain.model.OrderItems;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private int orderItemId;
    private int orderId;
    private int productId;
    private int quantity;
    private double price;

    public static OrderItemResponse from(OrderItems item) {
        return new OrderItemResponse(
                item.getOrderItemId(),
                item.getOrderId(),
                item.getProductId(),
                item.getQuantity(),
                item.getPrice());
    }
}
