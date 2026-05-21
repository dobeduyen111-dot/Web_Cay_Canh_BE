package ceb.service.implement;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import ceb.domain.model.Products;
import ceb.domain.res.PagedResponse;
import ceb.domain.res.ProductResponse;
import ceb.exception.CategoryNotFoundException;
import ceb.exception.InvalidProductCategoryException;
import ceb.exception.InvalidProductException;
import ceb.exception.InvalidProductPriceException;
import ceb.exception.InvalidProductStockException;
import ceb.exception.KeywordRequiredException;
import ceb.exception.LimitMustBePositiveException;
import ceb.exception.ProductNameRequiredException;
import ceb.exception.ProductNotFoundException;
import ceb.repository.CategoriesRepository;
import ceb.repository.ProductsRepository;
import ceb.service.service.ProductsService;

@Service
public class ProductsServiceImpl implements ProductsService {

    private final ProductsRepository productsRepository;
    private final CategoriesRepository categoriesRepository;

    public ProductsServiceImpl(ProductsRepository productsRepository, CategoriesRepository categoriesRepository) {
        this.productsRepository = productsRepository;
        this.categoriesRepository = categoriesRepository;
    }

    @Override
    public List<Products> findAll() {
        return productsRepository.findAll();
    }

    @Override
    public Products findById(int id) {
        try {
            return productsRepository.findById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw new ProductNotFoundException(id);
        }
    }

    @Override
    public List<Products> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new KeywordRequiredException();
        }
        return productsRepository.search(keyword.trim());
    }

    @Override
    public List<Products> findByCategory(int categoryId) {
        ensureCategoryExists(categoryId);
        return productsRepository.findByCategory(categoryId);
    }

    @Override
    public List<Products> getByCategoryLimit(int categoryId, int limit) {
        ensureCategoryExists(categoryId);
        if (limit <= 0) {
            throw new LimitMustBePositiveException();
        }
        return productsRepository.getByCategoryLimit(categoryId, limit);
    }

    @Override
    public Products create(Products product) {
        validateProduct(product);
        ensureCategoryExists(product.getCategoryId());
        productsRepository.save(product);
        return product;
    }

    @Override
    public Products update(int id, Products product) {
        findById(id);
        validateProduct(product);
        ensureCategoryExists(product.getCategoryId());
        product.setProductId(id);
        productsRepository.update(product);
        return product;
    }

    @Override
    public void delete(int id) {
        findById(id);
        productsRepository.delete(id);
    }

    @Override
    public PagedResponse<ProductResponse> findAdminPage(int page, int size, String keyword) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        long totalItems = productsRepository.countAdminProducts(keyword);
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / safeSize);

        return new PagedResponse<>(
                productsRepository.findAdminPage(offset, safeSize, keyword).stream().map(ProductResponse::from).toList(),
                safePage,
                safeSize,
                totalItems,
                totalPages,
                safePage < totalPages,
                safePage > 1);
    }

    private void validateProduct(Products product) {
        if (product == null) {
            throw new InvalidProductException();
        }
        if (product.getCategoryId() == null || product.getCategoryId() <= 0) {
            throw new InvalidProductCategoryException();
        }
        if (product.getProductName() == null || product.getProductName().isBlank()) {
            throw new ProductNameRequiredException();
        }
        if (product.getPrice() <= 0) {
            throw new InvalidProductPriceException();
        }
        if (product.getStock() < 0) {
            throw new InvalidProductStockException();
        }
    }

    private void ensureCategoryExists(int categoryId) {
        if (categoriesRepository.findById(categoryId) == null) {
            throw new CategoryNotFoundException(categoryId);
        }
    }
}
