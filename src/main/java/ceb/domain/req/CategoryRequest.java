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
public class CategoryRequest {

    @NotBlank(message = "Ten danh muc khong duoc de trong")
    @Size(max = 100, message = "Ten danh muc khong duoc vuot qua 100 ky tu")
    private String categoryName;

    @Size(max = 500, message = "Mo ta khong duoc vuot qua 500 ky tu")
    private String description;

    @Size(max = 100, message = "Icon khong duoc vuot qua 100 ky tu")
    private String icon;
}
