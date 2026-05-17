package ceb;

import java.util.Date;
import java.util.List;

import ceb.domain.entity.Users;
import ceb.domain.model.Cart;
import ceb.domain.model.CartItem;
import ceb.domain.model.Categories;
import ceb.domain.model.OrderItems;
import ceb.domain.model.Orders;
import ceb.domain.model.Payments;
import ceb.domain.model.Products;

public final class TestData {

    private TestData() {
    }

    public static Users user() {
        Users user = new Users();
        user.setUserId(7);
        user.setFullName("Nguyen Van A");
        user.setEmail("a@example.com");
        user.setPassword("$2a$10$encoded");
        user.setPhone("0900000000");
        user.setAddress("Da Lat");
        user.setRole("USER");
        user.setEnabled(true);
        return user;
    }

    public static Products product() {
        Products product = new Products();
        product.setProductId(11);
        product.setCategoryId(1);
        product.setProductName("Monstera");
        product.setDescription("Indoor plant");
        product.setCareGuide("Water weekly");
        product.setPrice(120_000);
        product.setStock(9);
        product.setImage("https://cdn.example.com/plant.jpg");
        product.setActive(true);
        product.setCreatedAt(new Date());
        return product;
    }

    public static Categories category() {
        Categories category = new Categories();
        category.setCategoryId(1);
        category.setCategoryName("Plants");
        category.setDescription("Green plants");
        category.setIcon("leaf");
        return category;
    }

    public static Cart cart() {
        Cart cart = new Cart();
        cart.setCartId(3);
        cart.setUserId(user().getUserId());
        return cart;
    }

    public static CartItem cartItem() {
        CartItem item = new CartItem();
        item.setCartItemId(5);
        item.setCartId(cart().getCartId());
        item.setProductId(product().getProductId());
        item.setQuantity(2);
        item.setProduct(product());
        return item;
    }

    public static Orders order() {
        Orders order = new Orders();
        order.setOrderId(17);
        order.setUserId(user().getUserId());
        order.setStatus("Cho xu ly");
        order.setTotalAmount(240_000);
        order.setShippingAddress("Da Lat");
        order.setNote("call first");
        order.setItems(List.of(orderItem()));
        return order;
    }

    public static OrderItems orderItem() {
        OrderItems item = new OrderItems();
        item.setOrderItemId(19);
        item.setOrderId(17);
        item.setProductId(product().getProductId());
        item.setQuantity(2);
        item.setPrice(120_000);
        return item;
    }

    public static Payments payment() {
        Payments payment = new Payments();
        payment.setPaymentId(23);
        payment.setOrderId(order().getOrderId());
        payment.setPaymentMethod("VNPAY");
        payment.setAmount(order().getTotalAmount());
        payment.setSuccessful(true);
        return payment;
    }
}
