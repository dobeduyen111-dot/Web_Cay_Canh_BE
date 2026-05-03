package ceb.service.implement;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ceb.domain.model.CartItem;
import ceb.domain.model.OrderItems;
import ceb.domain.model.Orders;
import ceb.exception.EmptyCartException;
import ceb.exception.OrderAccessDeniedException;
import ceb.exception.OrderNotFoundException;
import ceb.exception.OrderStatusRequiredException;
import ceb.exception.ShippingAddressRequiredException;
import ceb.repository.OrderItemsRepository;
import ceb.repository.OrdersRepository;
import ceb.service.service.CartService;
import ceb.service.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final CartService cartService;

    public OrderServiceImpl(
            OrdersRepository ordersRepository,
            OrderItemsRepository orderItemsRepository,
            CartService cartService) {
        this.ordersRepository = ordersRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.cartService = cartService;
    }

    @Override
    @Transactional
    public Orders checkout(int userId, String shippingAddress, String note) {
        if (shippingAddress == null || shippingAddress.isBlank()) {
            throw new ShippingAddressRequiredException();
        }

        List<CartItem> cartItems = cartService.getItems(userId);
        if (cartItems.isEmpty()) {
            throw new EmptyCartException();
        }

        Orders order = new Orders();
        order.setUserId(userId);
        order.setShippingAddress(shippingAddress.trim());
        order.setNote(note);
        order.setStatus("Cho xu ly");
        order.setTotalAmount(cartItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                .sum());

        int orderId = ordersRepository.createOrder(order);

        for (CartItem cartItem : cartItems) {
            OrderItems item = new OrderItems();
            item.setOrderId(orderId);
            item.setProductId(cartItem.getProductId());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getProduct().getPrice());
            orderItemsRepository.insertItem(item);
        }

        cartService.clear(userId);

        Orders createdOrder = findById(orderId);
        createdOrder.setItems(findItemsByOrderId(orderId));
        return createdOrder;
    }

    @Override
    public Orders findById(int orderId) {
        try {
            Orders order = ordersRepository.findById(orderId);
            order.setItems(findItemsByOrderId(orderId));
            return order;
        } catch (Exception ex) {
            throw new OrderNotFoundException(orderId);
        }
    }

    @Override
    public Orders findByIdForUser(int userId, int orderId) {
        Orders order = findById(orderId);
        validateOwner(userId, order);
        return order;
    }

    @Override
    public List<Orders> findByUserId(int userId) {
        return ordersRepository.findByUserId(userId);
    }

    @Override
    public List<Orders> findAll() {
        return ordersRepository.findAll();
    }

    @Override
    public List<OrderItems> findItemsByOrderId(int orderId) {
        return orderItemsRepository.findByOrderId(orderId);
    }

    @Override
    public List<OrderItems> findItemsByOrderIdForUser(int userId, int orderId) {
        findByIdForUser(userId, orderId);
        return findItemsByOrderId(orderId);
    }

    @Override
    public Orders updateStatus(int orderId, String status) {
        if (status == null || status.isBlank()) {
            throw new OrderStatusRequiredException();
        }
        findById(orderId);
        ordersRepository.updateStatus(orderId, status.trim());
        return findById(orderId);
    }

    @Override
    public void delete(int orderId) {
        findById(orderId);
        ordersRepository.delete(orderId);
    }

    private void validateOwner(int userId, Orders order) {
        if (order.getUserId() != userId) {
            throw new OrderAccessDeniedException();
        }
    }
}
