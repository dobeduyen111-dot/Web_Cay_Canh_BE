package ceb.domain.res;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminCustomerResponse {

    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String role;
    private boolean enabled;
    private LocalDateTime createdAt;
    private int orderCount;
    private double totalSpent;
}
