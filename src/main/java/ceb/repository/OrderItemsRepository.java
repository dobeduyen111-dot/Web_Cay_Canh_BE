package ceb.repository;

import ceb.domain.model.OrderItems;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class OrderItemsRepository {

    @Autowired
    private JdbcTemplate jdbc;

    public int insertItem(OrderItems item) {
        String sql = "INSERT INTO OrderItems (OrderId, ProductId, Quantity, Price) VALUES (?, ?, ?, ?)";
        return jdbc.update(sql,
                item.getOrderId(),
                item.getProductId(),
                item.getQuantity(),
                item.getPrice());
    }

    public List<OrderItems> findByOrderId(int orderId) {
        String sql = "SELECT * FROM OrderItems WHERE OrderId = ?";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(OrderItems.class), orderId);
    }
}
