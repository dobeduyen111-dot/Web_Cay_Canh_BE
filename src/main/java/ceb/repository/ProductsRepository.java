package ceb.repository;

import ceb.domain.model.Products;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProductsRepository {

    @Autowired
    private JdbcTemplate jdbc;

    public List<Products> findAll() {
        String sql = "SELECT * FROM Products";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Products.class));
    }

    public Products findById(int id) {
        String sql = "SELECT * FROM Products WHERE ProductId = ?";
        return jdbc.queryForObject(sql, new BeanPropertyRowMapper<>(Products.class), id);
    }

    public int save(Products p) {
        String sql = """
            INSERT INTO Products 
            (CategoryId, ProductName, Description, CareGuide, Price, Stock, Image, IsActive)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        return jdbc.update(sql,
            p.getCategoryId(),
            p.getProductName(),
            p.getDescription(),
            p.getCareGuide(),
            p.getPrice(),
            p.getStock(),
            p.getImage(),
            p.isActive() // Spring JDBC sẽ tự chuyển boolean Java sang boolean Postgres
        );
    }

    public int update(Products p) {
        String sql = """
            UPDATE Products SET 
            CategoryId=?, ProductName=?, Description=?, CareGuide=?, 
            Price=?, Stock=?, Image=?, IsActive=?
            WHERE ProductId=?
        """;

        return jdbc.update(sql,
            p.getCategoryId(),
            p.getProductName(),
            p.getDescription(),
            p.getCareGuide(),
            p.getPrice(),
            p.getStock(),
            p.getImage(),
            p.isActive(),
            p.getProductId()
        );
    }

    public int delete(int id) {
        return jdbc.update("DELETE FROM Products WHERE ProductId = ?", id);
    }

    public List<Products> search(String keyword) {
        String sql = """
            SELECT * FROM Products
            WHERE LOWER(ProductName) LIKE LOWER(?)
               OR LOWER(COALESCE(Description, '')) LIKE LOWER(?)
        """;
        String searchKeyword = "%" + keyword + "%";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Products.class), searchKeyword, searchKeyword);
    }

    public List<Products> getByCategoryLimit(int categoryId, int limit) {
        // Sửa: IsActive = true và thứ tự tham số ? (1 là categoryId, 2 là limit)
        String sql = "SELECT * FROM Products WHERE CategoryId = ? AND IsActive = true ORDER BY RANDOM() LIMIT ?";
        return jdbc.query(sql,
                new BeanPropertyRowMapper<>(Products.class),
                categoryId, limit); // Đổi chỗ categoryId lên trước limit
    }

    public List<Products> findByCategory(int categoryId) {
        // Sửa: IsActive = true
        String sql = "SELECT * FROM Products WHERE CategoryId = ? AND IsActive = true";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Products.class), categoryId);
    }
}
