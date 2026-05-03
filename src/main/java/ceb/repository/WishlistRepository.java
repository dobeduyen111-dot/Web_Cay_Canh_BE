package ceb.repository;

import java.util.List;
import ceb.domain.model.Products;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class WishlistRepository {

    @Autowired
    private JdbcTemplate jdbc;

    public void add(int userId, int productId) {
        String sql = """
            INSERT INTO Wishlist (UserId, ProductId)
            VALUES (?, ?)
            ON CONFLICT (UserId, ProductId) DO NOTHING
        """;
        jdbc.update(sql, userId, productId);
    }

    public List<Products> getWishlist(int userId) {
        String sql = """
            SELECT p.* FROM Wishlist w
            JOIN Products p ON w.ProductId = p.ProductId
            WHERE w.UserId = ?
        """;
        
        return jdbc.query(sql, (rs, rowNum) -> {
            Products p = new Products();
            p.setProductId(rs.getInt("ProductId"));
            p.setProductName(rs.getString("ProductName"));
            p.setPrice(rs.getDouble("Price"));
            p.setImage(rs.getString("Image"));
            return p;
        }, userId); 
    }

    public int countWishlist(int userId) {
        String sql = "SELECT COUNT(*) FROM Wishlist WHERE UserId = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    public void remove(int userId, int productId) {
        String sql = "DELETE FROM Wishlist WHERE UserId = ? AND ProductId = ?";
        jdbc.update(sql, userId, productId);
    }
}
