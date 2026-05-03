package ceb.domain.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartAddItemRequest {

    @NotNull(message = "Product id khong duoc de trong")
    @Positive(message = "Product id phai lon hon 0")
    private Integer productId;

    @Positive(message = "So luong phai lon hon 0")
    private Integer quantity;
}
