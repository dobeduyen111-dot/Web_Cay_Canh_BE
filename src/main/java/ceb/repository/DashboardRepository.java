package ceb.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardRepository {

    @Autowired
    private JdbcTemplate jdbc;

    public int getTotalOrders() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM Orders", Integer.class);
    }

    public double getTotalRevenue() {
        return jdbc.queryForObject("SELECT COALESCE(SUM(TotalAmount), 0) FROM Orders WHERE Status != 'Đã hủy'", Double.class);
    }

    public int getTotalCustomers() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM Users WHERE Role = 'USER'", Integer.class);
    }
}