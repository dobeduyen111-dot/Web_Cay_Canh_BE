package ceb.service.service;

import java.util.List;

import ceb.domain.model.OrderItems;
import ceb.domain.model.Orders;

public interface OrderService {

    Orders checkout(int userId, String shippingAddress, String note);

    Orders findById(int orderId);

    Orders findByIdForUser(int userId, int orderId);

    List<Orders> findByUserId(int userId);

    List<Orders> findAll();

    List<OrderItems> findItemsByOrderId(int orderId);

    List<OrderItems> findItemsByOrderIdForUser(int userId, int orderId);

    Orders updateStatus(int orderId, String status);

    void delete(int orderId);
}
