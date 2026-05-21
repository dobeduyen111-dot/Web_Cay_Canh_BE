package ceb.repository;

import ceb.domain.model.Products;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProductsRepository {

    private static final String PRODUCT_COLUMNS = """
        ProductId AS productId,
        CategoryId AS categoryId,
        ProductName AS productName,
        Description AS description,
        CareGuide AS careGuide,
        Price AS price,
        Stock AS stock,
        Image AS image,
        IsActive AS active,
        CreatedAt AS createdAt
    """;

    @Autowired
    private JdbcTemplate jdbc;

    public List<Products> findAll() {
        String sql = "SELECT " + PRODUCT_COLUMNS + " FROM Products WHERE IsActive = true ORDER BY ProductId DESC";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Products.class));
    }

    public Products findById(int id) {
        String sql = "SELECT " + PRODUCT_COLUMNS + " FROM Products WHERE ProductId = ?";
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
            SELECT %s FROM Products
            WHERE IsActive = true
              AND (
                LOWER(ProductName) LIKE LOWER(?)
                OR LOWER(COALESCE(Description, '')) LIKE LOWER(?)
              )
            ORDER BY ProductId DESC
        """;
        String searchKeyword = "%" + keyword + "%";
        return jdbc.query(sql.formatted(PRODUCT_COLUMNS), new BeanPropertyRowMapper<>(Products.class), searchKeyword, searchKeyword);
    }

    public List<Products> getByCategoryLimit(int categoryId, int limit) {
        String sql = "SELECT " + PRODUCT_COLUMNS + " FROM Products WHERE CategoryId = ? AND IsActive = true ORDER BY RANDOM() LIMIT ?";
        return jdbc.query(sql,
                new BeanPropertyRowMapper<>(Products.class),
                categoryId, limit);
    }

    public List<Products> findByCategory(int categoryId) {
        String sql = "SELECT " + PRODUCT_COLUMNS + " FROM Products WHERE CategoryId = ? AND IsActive = true ORDER BY ProductId DESC";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Products.class), categoryId);
    }

    public List<Products> findAdminPage(int offset, int limit, String keyword) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        String sql = """
            SELECT %s FROM Products
            %s
            ORDER BY ProductId DESC
            LIMIT ? OFFSET ?
        """;

        String whereClause = hasKeyword
                ? "WHERE LOWER(ProductName) LIKE LOWER(?) OR LOWER(COALESCE(Description, '')) LIKE LOWER(?)"
                : "";

        if (hasKeyword) {
            String searchKeyword = "%" + keyword.trim() + "%";
            return jdbc.query(sql.formatted(PRODUCT_COLUMNS, whereClause),
                    new BeanPropertyRowMapper<>(Products.class),
                    searchKeyword,
                    searchKeyword,
                    limit,
                    offset);
        }

        return jdbc.query(sql.formatted(PRODUCT_COLUMNS, whereClause),
                new BeanPropertyRowMapper<>(Products.class),
                limit,
                offset);
    }

    public long countAdminProducts(String keyword) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        String sql = """
            SELECT COUNT(*) FROM Products
            %s
        """;

        String whereClause = hasKeyword
                ? "WHERE LOWER(ProductName) LIKE LOWER(?) OR LOWER(COALESCE(Description, '')) LIKE LOWER(?)"
                : "";

        if (hasKeyword) {
            String searchKeyword = "%" + keyword.trim() + "%";
            Long count = jdbc.queryForObject(sql.formatted(whereClause), Long.class, searchKeyword, searchKeyword);
            return count == null ? 0L : count;
        }

        Long count = jdbc.queryForObject(sql.formatted(whereClause), Long.class);
        return count == null ? 0L : count;
    }
}
