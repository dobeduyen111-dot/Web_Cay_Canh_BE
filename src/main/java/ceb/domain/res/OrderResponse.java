package ceb.domain.res;

import java.util.Date;
import java.util.List;

import ceb.domain.model.Orders;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private int orderId;
    private int userId;
    private Date orderDate;
    private String status;
    private double totalAmount;
    private String shippingAddress;
    private String note;
    private String fullName;
    private List<OrderItemResponse> items;

    public static OrderResponse from(Orders order) {
        List<OrderItemResponse> itemResponses = order.getItems() == null
                ? List.of()
                : order.getItems().stream().map(OrderItemResponse::from).toList();

        return new OrderResponse(
                order.getOrderId(),
                order.getUserId(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getNote(),
                order.getFullName(),
                itemResponses);
    }
}
