package ceb.domain.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterRequest {

    @NotBlank(message = "Ho ten khong duoc de trong")
    @Size(max = 100, message = "Ho ten khong duoc vuot qua 100 ky tu")
    private String fullName;

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong dung dinh dang")
    @Size(max = 150, message = "Email khong duoc vuot qua 150 ky tu")
    private String email;

    @NotBlank(message = "Mat khau khong duoc de trong")
    @ceb.validation.ValidPassword
    private String password;

    @ceb.validation.ValidPhoneNumber
    private String phone;


    @Size(max = 255, message = "Dia chi khong duoc vuot qua 255 ky tu")
    private String address;
}
