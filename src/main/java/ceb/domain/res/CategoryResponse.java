package ceb.domain.res;

import ceb.domain.model.Categories;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Integer categoryId;
    private String categoryName;
    private String description;
    private String icon;

    public static CategoryResponse from(Categories category) {
        return new CategoryResponse(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getDescription(),
                category.getIcon());
    }
}
