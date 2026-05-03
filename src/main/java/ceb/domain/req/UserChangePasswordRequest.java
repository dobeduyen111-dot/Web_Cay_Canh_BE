package ceb.domain.req;

import ceb.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordRequest {

    @NotBlank(message = "Mat khau hien tai khong duoc de trong")
    private String oldPassword;

    @NotBlank(message = "Mat khau moi khong duoc de trong")
    @ValidPassword
    private String newPassword;
}
