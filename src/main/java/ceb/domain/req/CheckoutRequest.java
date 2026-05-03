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
public class CheckoutRequest {

    @NotBlank(message = "Dia chi giao hang khong duoc de trong")
    @Size(max = 255, message = "Dia chi giao hang khong duoc vuot qua 255 ky tu")
    private String shippingAddress;

    @Size(max = 1000, message = "Ghi chu khong duoc vuot qua 1000 ky tu")
    private String note;
}
