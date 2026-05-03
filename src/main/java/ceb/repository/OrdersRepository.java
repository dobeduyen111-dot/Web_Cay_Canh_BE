package ceb.repository;

import ceb.domain.model.Orders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Repository
public class OrdersRepository {

    @Autowired
    private JdbcTemplate jdbc;

    public int createOrder(Orders o) {
        String sql = "INSERT INTO Orders (UserId, Status, TotalAmount, ShippingAddress, Note) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"orderid"});
            ps.setInt(1, o.getUserId());
            ps.setString(2, o.getStatus() == null ? "Chờ xử lý" : o.getStatus());
            ps.setDouble(3, o.getTotalAmount());
            ps.setString(4, o.getShippingAddress());
            ps.setString(5, o.getNote());
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public Orders findById(int orderId) {
        String sql = "SELECT * FROM Orders WHERE OrderId = ?";
        return jdbc.queryForObject(sql, new BeanPropertyRowMapper<>(Orders.class), orderId);
    }

    public List<Orders> findByUserId(int userId) {
        String sql = "SELECT * FROM Orders WHERE UserId = ? ORDER BY OrderDate DESC";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Orders.class), userId);
    }
    public List<Orders> findAll() {
        String sql = """
            SELECT o.*, u.FullName 
            FROM Orders o
            JOIN Users u ON o.UserId = u.UserId
            ORDER BY o.OrderDate DESC
        """;
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Orders.class));
    }
    public void updateStatus(int orderId, String status) {
        String sql = "UPDATE Orders SET Status = ? WHERE OrderId = ?";
        jdbc.update(sql, status, orderId);
    }
    public void delete(int orderId) {
        String sqlDetails = "DELETE FROM OrderItems WHERE OrderId = ?";
        jdbc.update(sqlDetails, orderId);
        String sqlOrder = "DELETE FROM Orders WHERE OrderId = ?";
        jdbc.update(sqlOrder, orderId);
    }
}