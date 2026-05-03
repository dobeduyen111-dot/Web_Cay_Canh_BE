package ceb.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ceb.domain.model.Products;
import ceb.domain.req.ProductRequest;
import ceb.domain.res.MessageResponse;
import ceb.domain.res.ProductResponse;
import ceb.service.service.CloudinaryService;
import ceb.service.service.ProductsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/products")
public class ProductsController {

    private final ProductsService productsService;
    private final CloudinaryService cloudinaryService;

    public ProductsController(ProductsService productsService, CloudinaryService cloudinaryService) {
        this.productsService = productsService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public List<ProductResponse> findAll() {
        return productsService.findAll().stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProductResponse findById(
            @PathVariable @Positive(message = "Product id phai lon hon 0") int id) {
        return ProductResponse.from(productsService.findById(id));
    }

    @GetMapping("/search")
    public List<ProductResponse> search(
            @RequestParam
            @NotBlank(message = "Keyword khong duoc de trong")
            @Size(max = 100, message = "Keyword khong duoc vuot qua 100 ky tu")
            String keyword) {
        return productsService.search(keyword).stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductResponse> findByCategory(
            @PathVariable @Positive(message = "Category id phai lon hon 0") int categoryId) {
        return productsService.findByCategory(categoryId).stream().map(ProductResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @ModelAttribute ProductRequest request) {
        return ProductResponse.from(productsService.create(toProduct(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse update(
            @PathVariable @Positive(message = "Product id phai lon hon 0") int id,
            @Valid @ModelAttribute ProductRequest request) {
        Products existingProduct = productsService.findById(id);
        return ProductResponse.from(productsService.update(id, toProduct(request, existingProduct)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MessageResponse delete(
            @PathVariable @Positive(message = "Product id phai lon hon 0") int id) {
        productsService.delete(id);
        return new MessageResponse("Xoa san pham thanh cong");
    }

    private Products toProduct(ProductRequest request) {
        Products product = new Products();
        product.setCategoryId(request.getCategoryId());
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setCareGuide(request.getCareGuide());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setActive(request.isActive());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(request.getImage()).getUrl();
            product.setImage(imageUrl);
        } else {
            product.setImage(null);
        }

        return product;
    }

    private Products toProduct(ProductRequest request, Products existingProduct) {
        Products product = new Products();
        product.setCategoryId(request.getCategoryId());
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setCareGuide(request.getCareGuide());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setActive(request.isActive());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(request.getImage()).getUrl();
            product.setImage(imageUrl);
        } else {
            product.setImage(existingProduct.getImage()); // keep existing image
        }

        return product;
    }
}
