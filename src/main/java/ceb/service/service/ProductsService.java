package ceb.service.service;

import java.util.List;

import ceb.domain.model.Products;

public interface ProductsService {

    List<Products> findAll();

    Products findById(int id);

    List<Products> search(String keyword);

    List<Products> findByCategory(int categoryId);

    List<Products> getByCategoryLimit(int categoryId, int limit);

    Products create(Products product);

    Products update(int id, Products product);

    void delete(int id);
}
