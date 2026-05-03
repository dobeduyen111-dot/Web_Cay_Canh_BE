package ceb.repository;

import ceb.domain.model.Payments;
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
public class PaymentsRepository {

    @Autowired
    private JdbcTemplate jdbc;

    public int create(Payments p) {
        String sql = "INSERT INTO Payments (OrderId, PaymentMethod, Amount, IsSuccessful) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"paymentid"});
            ps.setInt(1, p.getOrderId());
            ps.setString(2, p.getPaymentMethod());
            ps.setDouble(3, p.getAmount());
            ps.setBoolean(4, p.isSuccessful());
            return ps;
        }, keyHolder);
        
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public Payments findById(int paymentId) {
        String sql = "SELECT * FROM Payments WHERE PaymentId = ?";
        return jdbc.queryForObject(sql, new BeanPropertyRowMapper<>(Payments.class), paymentId);
    }

    public List<Payments> findByOrderId(int orderId) {
        String sql = "SELECT * FROM Payments WHERE OrderId = ?";
        // Sửa: Truyền tham số trực tiếp
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Payments.class), orderId);
    }
}
