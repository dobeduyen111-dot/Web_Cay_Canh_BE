package ceb.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ceb.domain.model.Categories;

@Repository
public class CategoriesRepository {

    @Autowired
    private JdbcTemplate jdbc;

    private final RowMapper<Categories> mapper = (rs, rowNum) -> {
        Categories c = new Categories();
        c.setCategoryId(rs.getInt("CategoryId"));
        c.setCategoryName(rs.getString("CategoryName"));
        c.setDescription(rs.getString("Description"));
        c.setIcon(rs.getString("Icon"));
        return c;
    };

    public List<Categories> findAll() {
        return jdbc.query("SELECT * FROM Categories", mapper);
    }

    public Categories findById(int id) {

        List<Categories> list = jdbc.query("SELECT * FROM Categories WHERE CategoryId = ?", mapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int save(Categories c) {
        if (c.getCategoryId() == null || c.getCategoryId() == 0) { 
            String sql = "INSERT INTO Categories (CategoryName, Description, Icon) VALUES (?,?,?)";
            return jdbc.update(sql, c.getCategoryName(), c.getDescription(), c.getIcon());
        } else {
            String sql = "UPDATE Categories SET CategoryName=?, Description=?, Icon=? WHERE CategoryId=?";
            return jdbc.update(sql, c.getCategoryName(), c.getDescription(), c.getIcon(), c.getCategoryId());
        }
    }

    public int delete(int id) {
        return jdbc.update("DELETE FROM Categories WHERE CategoryId = ?", id);
    }
}