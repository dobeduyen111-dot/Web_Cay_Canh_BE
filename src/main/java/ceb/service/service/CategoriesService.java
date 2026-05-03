package ceb.service.service;

import java.util.List;

import ceb.domain.model.Categories;

public interface CategoriesService {

    List<Categories> findAll();

    Categories findById(int id);

    Categories create(Categories category);

    Categories update(int id, Categories category);

    void delete(int id);
}
