package ceb.domain.req;

import ceb.validation.ValidPrice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductJsonRequest {

    @NotNull(message = "Category id khong duoc de trong")
    @Positive(message = "Category id phai lon hon 0")
    private Integer categoryId;

    @NotBlank(message = "Ten san pham khong duoc de trong")
    @Size(max = 150, message = "Ten san pham khong duoc vuot qua 150 ky tu")
    private String productName;

    @Size(max = 5000, message = "Mo ta khong duoc vuot qua 5000 ky tu")
    private String description;

    @Size(max = 5000, message = "Huong dan cham soc khong duoc vuot qua 5000 ky tu")
    private String careGuide;

    @NotNull(message = "Gia san pham khong duoc de trong")
    @ValidPrice
    private Double price;

    @NotNull(message = "So luong ton kho khong duoc de trong")
    @PositiveOrZero(message = "So luong ton kho khong duoc am")
    private Integer stock;

    private String image;

    private boolean active;
}
