package ceb.domain.req;

import ceb.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong dung dinh dang")
    @Size(max = 150, message = "Email khong duoc vuot qua 150 ky tu")
    private String email;

    @NotBlank(message = "Mat khau moi khong duoc de trong")
    @ValidPassword
    private String newPassword;
}
