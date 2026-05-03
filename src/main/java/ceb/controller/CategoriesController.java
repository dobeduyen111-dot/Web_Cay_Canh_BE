package ceb.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ceb.domain.model.Categories;
import ceb.domain.req.CategoryRequest;
import ceb.domain.res.CategoryResponse;
import ceb.domain.res.MessageResponse;
import ceb.service.service.CategoriesService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@Validated
@RestController
@RequestMapping("/api/categories")
public class CategoriesController {

    private final CategoriesService categoriesService;

    public CategoriesController(CategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoriesService.findAll().stream().map(CategoryResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CategoryResponse findById(
            @PathVariable @Positive(message = "Category id phai lon hon 0") int id) {
        return CategoryResponse.from(categoriesService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return CategoryResponse.from(categoriesService.create(toCategory(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse update(
            @PathVariable @Positive(message = "Category id phai lon hon 0") int id,
            @Valid @RequestBody CategoryRequest request) {
        return CategoryResponse.from(categoriesService.update(id, toCategory(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MessageResponse delete(
            @PathVariable @Positive(message = "Category id phai lon hon 0") int id) {
        categoriesService.delete(id);
        return new MessageResponse("Xoa danh muc thanh cong");
    }

    private Categories toCategory(CategoryRequest request) {
        Categories category = new Categories();
        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        return category;
    }
}
