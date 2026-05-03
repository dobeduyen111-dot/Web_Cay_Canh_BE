package ceb.domain.res;

import java.time.LocalDateTime;

import ceb.domain.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String role;
    private boolean enabled;
    private LocalDateTime createdAt;

    public static UserResponse from(Users user) {
        return new UserResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt());
    }
}
