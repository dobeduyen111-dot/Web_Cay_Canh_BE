package ceb.repository;


import ceb.domain.model.Products;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import ceb.domain.model.CartItem;

@Repository
public class CartItemsRepository {

    @Autowired
    private JdbcTemplate jdbc;

    public CartItem findItem(int cartId, int productId) {
        String sql = """
            SELECT ci.CartItemId, ci.CartId, ci.ProductId, ci.Quantity,
                   p.ProductName, p.Price, p.Image
            FROM CartItems ci
            JOIN Products p ON ci.ProductId = p.ProductId
            WHERE ci.CartId = ? AND ci.ProductId = ?
        """;

        List<CartItem> list = jdbc.query(sql, (rs, rowNum) -> {
            CartItem ci = new CartItem();
            ci.setCartItemId(rs.getInt("CartItemId"));
            ci.setCartId(rs.getInt("CartId"));
            ci.setProductId(rs.getInt("ProductId"));
            ci.setQuantity(rs.getInt("Quantity"));

            Products p = new Products();
            p.setProductId(rs.getInt("ProductId"));
            p.setProductName(rs.getString("ProductName"));
            p.setPrice(rs.getDouble("Price"));
            p.setImage(rs.getString("Image"));

            ci.setProduct(p);
            return ci;
        }, cartId, productId);

        return list.isEmpty() ? null : list.get(0);
    }

    public List<CartItem> findAll(int cartId) {
        String sql = """
            SELECT ci.CartItemId, ci.CartId, ci.ProductId, ci.Quantity,
                   p.ProductName, p.Price, p.Image
            FROM CartItems ci
            JOIN Products p ON ci.ProductId = p.ProductId
            WHERE ci.CartId = ?
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            CartItem ci = new CartItem();
            ci.setCartItemId(rs.getInt("CartItemId"));
            ci.setCartId(rs.getInt("CartId"));
            ci.setProductId(rs.getInt("ProductId"));
            ci.setQuantity(rs.getInt("Quantity"));

            Products p = new Products();
            p.setProductId(rs.getInt("ProductId"));
            p.setProductName(rs.getString("ProductName"));
            p.setPrice(rs.getDouble("Price"));
            p.setImage(rs.getString("Image"));

            ci.setProduct(p);
            return ci;
        }, cartId);
    }

    public CartItem findById(int cartItemId) {
        String sql = """
            SELECT ci.CartItemId, ci.CartId, ci.ProductId, ci.Quantity,
                   p.ProductName, p.Price, p.Image
            FROM CartItems ci
            JOIN Products p ON ci.ProductId = p.ProductId
            WHERE ci.CartItemId = ?
        """;

        List<CartItem> list = jdbc.query(sql, (rs, rowNum) -> {
            CartItem ci = new CartItem();
            ci.setCartItemId(rs.getInt("CartItemId"));
            ci.setCartId(rs.getInt("CartId"));
            ci.setProductId(rs.getInt("ProductId"));
            ci.setQuantity(rs.getInt("Quantity"));

            Products p = new Products();
            p.setProductId(rs.getInt("ProductId"));
            p.setProductName(rs.getString("ProductName"));
            p.setPrice(rs.getDouble("Price"));
            p.setImage(rs.getString("Image"));

            ci.setProduct(p);
            return ci;
        }, cartItemId);

        return list.isEmpty() ? null : list.get(0);
    }

    public void addItem(int cartId, int productId, int quantity) {
        // Trước đây: VALUES (?, ?, 1) -> Nó luôn thêm 1
        // Bây giờ: VALUES (?, ?, ?) -> Nó sẽ thêm số lượng bạn muốn
        String sql = "INSERT INTO CartItems (CartId, ProductId, Quantity) VALUES (?, ?, ?)";
        jdbc.update(sql, cartId, productId, quantity);
    }

    public void updateQuantity(int cartItemId, int quantity) {
        String sql = "UPDATE CartItems SET Quantity = ? WHERE CartItemId = ?";
        jdbc.update(sql, quantity, cartItemId);
    }

    public void delete(int cartItemId) {
        String sql = "DELETE FROM CartItems WHERE CartItemId = ?";
        jdbc.update(sql, cartItemId);
    }

    public void clear(int cartId) {
        String sql = "DELETE FROM CartItems WHERE CartId = ?";
        jdbc.update(sql, cartId);
    }
}
