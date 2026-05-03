package ceb.domain.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Order id khong duoc de trong")
    @Positive(message = "Order id phai lon hon 0")
    private Integer orderId;

    @NotBlank(message = "Phuong thuc thanh toan khong duoc de trong")
    @Size(max = 50, message = "Phuong thuc thanh toan khong duoc vuot qua 50 ky tu")
    private String paymentMethod;

    @Positive(message = "So tien thanh toan phai lon hon 0")
    private Double amount;

    private Boolean successful;
}
