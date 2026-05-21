package ceb.repository;

import ceb.domain.model.OrderItems;
import ceb.domain.model.Products;
import org.springframework.beans.factory.annotation.Autowired;
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
        String sql = """
            SELECT
                oi.OrderItemId,
                oi.OrderId,
                oi.ProductId,
                oi.Quantity,
                oi.Price,
                p.ProductId AS productProductId,
                p.CategoryId AS productCategoryId,
                p.ProductName AS productName,
                p.Description AS productDescription,
                p.CareGuide AS productCareGuide,
                p.Price AS productPrice,
                p.Stock AS productStock,
                p.Image AS productImage,
                p.IsActive AS productActive,
                p.CreatedAt AS productCreatedAt
            FROM OrderItems oi
            LEFT JOIN Products p ON oi.ProductId = p.ProductId
            WHERE oi.OrderId = ?
        """;
        return jdbc.query(sql, (rs, rowNum) -> {
            OrderItems item = new OrderItems();
            item.setOrderItemId(rs.getInt("OrderItemId"));
            item.setOrderId(rs.getInt("OrderId"));
            item.setProductId(rs.getInt("ProductId"));
            item.setQuantity(rs.getInt("Quantity"));
            item.setPrice(rs.getDouble("Price"));

            if (rs.getObject("productProductId") != null) {
                Products product = new Products();
                product.setProductId(rs.getInt("productProductId"));
                product.setCategoryId(rs.getInt("productCategoryId"));
                product.setProductName(rs.getString("productName"));
                product.setDescription(rs.getString("productDescription"));
                product.setCareGuide(rs.getString("productCareGuide"));
                product.setPrice(rs.getDouble("productPrice"));
                product.setStock(rs.getInt("productStock"));
                product.setImage(rs.getString("productImage"));
                product.setActive(rs.getBoolean("productActive"));
                product.setCreatedAt(rs.getTimestamp("productCreatedAt"));
                item.setProduct(product);
            }

            return item;
        }, orderId);
    }
}
