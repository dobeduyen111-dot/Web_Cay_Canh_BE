package ceb.domain.req;

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
public class AdminUpdateOrderStatusRequest {

    @NotBlank(message = "Trang thai khong duoc de trong")
    @Size(max = 50, message = "Trang thai khong duoc vuot qua 50 ky tu")
    private String status;
}
