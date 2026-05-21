package ceb.domain.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateUserRoleRequest {

    @NotBlank(message = "Role khong duoc de trong")
    @Size(max = 20, message = "Role khong duoc vuot qua 20 ky tu")
    private String role;
}
