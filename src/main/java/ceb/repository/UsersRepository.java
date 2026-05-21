package ceb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ceb.domain.entity.Users;
import ceb.domain.res.AdminCustomerResponse;

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

    private final RowMapper<AdminCustomerResponse> adminCustomerMapper = (rs, rowNum) -> new AdminCustomerResponse(
            rs.getInt("userId"),
            rs.getString("fullName"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("address"),
            rs.getString("role"),
            rs.getBoolean("enabled"),
            rs.getTimestamp("createdAt") == null ? null : rs.getTimestamp("createdAt").toLocalDateTime(),
            rs.getInt("orderCount"),
            rs.getDouble("totalSpent"));

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
        return jdbc.query("SELECT * FROM Users ORDER BY CreatedAt DESC NULLS LAST, UserId DESC", userMapper);
    }

    // 7. Xóa User
    public int deleteById(int id) {
        String sql = "DELETE FROM Users WHERE UserId = ?";
        return jdbc.update(sql, id);
    }

    public int updateRole(int userId, String role) {
        String sql = "UPDATE Users SET Role = ? WHERE UserId = ?";
        return jdbc.update(sql, role, userId);
    }

    public int updateEnabled(int userId, boolean enabled) {
        String sql = "UPDATE Users SET Enabled = ? WHERE UserId = ?";
        return jdbc.update(sql, enabled, userId);
    }

    public long countEnabledAdmins() {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Users WHERE UPPER(Role) = 'ADMIN' AND Enabled = true",
                Long.class);
        return count == null ? 0L : count;
    }

    public List<AdminCustomerResponse> findAdminCustomersPage(int offset, int limit, String keyword) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        String sql = """
            SELECT
                u.UserId AS userId,
                u.FullName AS fullName,
                u.Email AS email,
                u.Phone AS phone,
                u.Address AS address,
                u.Role AS role,
                u.Enabled AS enabled,
                u.CreatedAt AS createdAt,
                COALESCE(SUM(CASE
                    WHEN LOWER(COALESCE(o.Status, '')) NOT IN ('cancelled', 'canceled', 'cancel', 'da huy', 'đã hủy')
                    THEN 1
                    ELSE 0
                END), 0) AS orderCount,
                COALESCE(SUM(CASE
                    WHEN LOWER(COALESCE(o.Status, '')) NOT IN ('cancelled', 'canceled', 'cancel', 'da huy', 'đã hủy')
                    THEN o.TotalAmount
                    ELSE 0
                END), 0) AS totalSpent
            FROM Users u
            LEFT JOIN Orders o ON o.UserId = u.UserId
            %s
            GROUP BY u.UserId, u.FullName, u.Email, u.Phone, u.Address, u.Role, u.Enabled, u.CreatedAt
            ORDER BY u.CreatedAt DESC NULLS LAST, u.UserId DESC
            LIMIT ? OFFSET ?
        """;

        String whereClause = hasKeyword
                ? "WHERE LOWER(COALESCE(u.FullName, '')) LIKE LOWER(?) OR LOWER(COALESCE(u.Email, '')) LIKE LOWER(?) OR COALESCE(u.Phone, '') LIKE ?"
                : "";

        if (hasKeyword) {
            String normalized = "%" + keyword.trim() + "%";
            return jdbc.query(sql.formatted(whereClause), adminCustomerMapper, normalized, normalized, normalized, limit, offset);
        }

        return jdbc.query(sql.formatted(whereClause), adminCustomerMapper, limit, offset);
    }

    public long countAdminCustomers(String keyword) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        String sql = """
            SELECT COUNT(*) FROM Users u
            %s
        """;

        String whereClause = hasKeyword
                ? "WHERE LOWER(COALESCE(u.FullName, '')) LIKE LOWER(?) OR LOWER(COALESCE(u.Email, '')) LIKE LOWER(?) OR COALESCE(u.Phone, '') LIKE ?"
                : "";

        if (hasKeyword) {
            String normalized = "%" + keyword.trim() + "%";
            Long count = jdbc.queryForObject(sql.formatted(whereClause), Long.class, normalized, normalized, normalized);
            return count == null ? 0L : count;
        }

        Long count = jdbc.queryForObject(sql.formatted(whereClause), Long.class);
        return count == null ? 0L : count;
    }
}
