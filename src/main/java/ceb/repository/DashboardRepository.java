package ceb.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ceb.domain.res.DashboardPointResponse;

@Repository
public class DashboardRepository {

    @Autowired
    private JdbcTemplate jdbc;

    public int getTotalOrders() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM Orders", Integer.class);
        return count == null ? 0 : count;
    }

    public int getTotalOrders(Timestamp start, Timestamp end) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Orders WHERE OrderDate >= ? AND OrderDate < ?",
                Integer.class,
                start,
                end);
        return count == null ? 0 : count;
    }

    public double getTotalRevenue(Timestamp start, Timestamp end) {
        Double revenue = jdbc.queryForObject("""
            SELECT COALESCE(SUM(TotalAmount), 0)
            FROM Orders
            WHERE OrderDate >= ? AND OrderDate < ?
              AND LOWER(COALESCE(Status, '')) NOT IN ('cancelled', 'canceled', 'cancel', 'da huy', 'đã hủy')
        """, Double.class, start, end);
        return revenue == null ? 0D : revenue;
    }

    public double getTotalRevenue() {
        Double revenue = jdbc.queryForObject("""
            SELECT COALESCE(SUM(TotalAmount), 0)
            FROM Orders
            WHERE LOWER(COALESCE(Status, '')) NOT IN ('cancelled', 'canceled', 'cancel', 'da huy', 'đã hủy')
        """, Double.class);
        return revenue == null ? 0D : revenue;
    }

    public int getTotalCustomers(Timestamp start, Timestamp end) {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(DISTINCT UserId)
            FROM Orders
            WHERE OrderDate >= ? AND OrderDate < ?
        """, Integer.class, start, end);
        return count == null ? 0 : count;
    }

    public int getTotalCustomers() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM Users WHERE Role = 'USER'", Integer.class);
        return count == null ? 0 : count;
    }

    public int getTotalProducts() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Products WHERE IsActive = true",
                Integer.class);
        return count == null ? 0 : count;
    }

    public List<DashboardPointResponse> getRevenueSeries(Timestamp start, Timestamp end) {
        String sql = """
            SELECT
                CAST(OrderDate AS DATE) AS orderDay,
                COALESCE(SUM(CASE
                    WHEN LOWER(COALESCE(Status, '')) NOT IN ('cancelled', 'canceled', 'cancel', 'da huy', 'đã hủy')
                    THEN TotalAmount
                    ELSE 0
                END), 0) AS revenue,
                COUNT(*) AS orders
            FROM Orders
            WHERE OrderDate >= ? AND OrderDate < ?
            GROUP BY CAST(OrderDate AS DATE)
            ORDER BY orderDay ASC
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            LocalDate day = rs.getDate("orderDay").toLocalDate();
            return new DashboardPointResponse(
                    day.toString(),
                    "",
                    rs.getDouble("revenue"),
                    rs.getInt("orders"));
        }, start, end);
    }
}
