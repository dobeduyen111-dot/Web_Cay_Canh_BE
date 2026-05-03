package ceb.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ceb.domain.model.Cart;

@Repository
public class CartRepository {

    @Autowired
    private JdbcTemplate jdbc;

    public Cart getByUserId(int userId) {
        String sql = "SELECT * FROM Cart WHERE UserId = ?";
        List<Cart> list = jdbc.query(sql, new BeanPropertyRowMapper<>(Cart.class), userId);
        return list.isEmpty() ? null : list.get(0);
    }

    public int createCart(int userId) {
        String sql = "INSERT INTO Cart (UserId) VALUES (?)";
        jdbc.update(sql, userId);
        
        String fetch = "SELECT * FROM Cart WHERE UserId = ?";
        return jdbc.queryForObject(fetch, new BeanPropertyRowMapper<>(Cart.class), userId).getCartId();
    }
}