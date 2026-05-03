package ceb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ceb.domain.entity.Users;

@Repository
public class UsersRepository {

    @Autowired
    private JdbcTemplate jdbc;

    


    // Mapper chuyển đổi dữ liệu từ DB sang Object Users
    private final RowMapper<Users> userMapper = (rs, rowNum) -> {
        Users u = new Users();
        u.setUserId(rs.getInt("UserId"));
        u.setFullName(rs.getString("FullName"));
        u.setEmail(rs.getString("Email"));
        u.setPassword(rs.getString("Password"));
        u.setPhone(rs.getString("Phone"));
        u.setAddress(rs.getString("Address"));
        u.setRole(rs.getString("Role"));
        u.setEnabled(rs.getBoolean("Enabled"));
        if (rs.getTimestamp("CreatedAt") != null) {
            u.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
        }
        return u;
    };

    // 1. Dùng cho MyUserDetailsService (Đăng nhập)
    public Optional<Users> findByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE Email = ?";
        return jdbc.query(sql, userMapper, username).stream().findFirst();
    }

    // 2. Dùng cho UserService (LỖI BẠN VỪA GẶP)
    public Optional<Integer> getUserIdByEmail(String email) {
        try {
            String sql = "SELECT UserId FROM Users WHERE Email = ?";
            Integer id = jdbc.queryForObject(sql, Integer.class, email);
            return Optional.ofNullable(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // 3. Dùng cho AdminController (Cập nhật mật khẩu)
    public int updatePassword(int userId, String newPassword) {
        String sql = "UPDATE Users SET Password = ? WHERE UserId = ?";
        return jdbc.update(sql, newPassword, userId);
    }

    // 4. Tìm theo Email
    public Optional<Users> findByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE Email = ?";
        return jdbc.query(sql, userMapper, email).stream().findFirst();
    }

    public Optional<Users> findByPhone(String phone) {
        String sql = "SELECT * FROM Users WHERE Phone = ?";
        return jdbc.query(sql, userMapper, phone).stream().findFirst();
    }

    public Optional<Users> findById(int userId) {
        String sql = "SELECT * FROM Users WHERE UserId = ?";
        return jdbc.query(sql, userMapper, userId).stream().findFirst();
    }

    public int updateProfile(int userId, String fullName, String email, String phone, String address) {
        String sql = """
            UPDATE Users
            SET FullName = ?, Email = ?, Phone = ?, Address = ?
            WHERE UserId = ?
        """;
        return jdbc.update(sql, fullName, email, phone, address, userId);
    }

    // 5. Lưu User mới
    public int save(Users user) {
        String sql = """
            INSERT INTO Users (FullName, Email, Password, Phone, Address, Role, Enabled)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        return jdbc.update(sql,
                user.getFullName(),
                user.getEmail(),
                user.getPassword(),
                user.getPhone(),
                user.getAddress(),
                user.getRole(),
                user.isEnabled()
        );
    }

    // 6. Lấy tất cả danh sách
    public List<Users> findAlls() {
        return jdbc.query("SELECT * FROM Users", userMapper);
    }

    // 7. Xóa User
    public int deleteById(int id) {
        String sql = "DELETE FROM Users WHERE UserId = ?";
        return jdbc.update(sql, id);
    }
}
