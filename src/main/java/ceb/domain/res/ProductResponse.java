package ceb.domain.res;

import java.util.Date;

import ceb.domain.model.Products;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Integer productId;
    private Integer categoryId;
    private String productName;
    private String description;
    private String careGuide;
    private double price;
    private int stock;
    private String image;
    private boolean active;
    private Date createdAt;

    public static ProductResponse from(Products product) {
        return new ProductResponse(
                product.getProductId(),
                product.getCategoryId(),
                product.getProductName(),
                product.getDescription(),
                product.getCareGuide(),
                product.getPrice(),
                product.getStock(),
                product.getImage(),
                product.isActive(),
                product.getCreatedAt());
    }
}
