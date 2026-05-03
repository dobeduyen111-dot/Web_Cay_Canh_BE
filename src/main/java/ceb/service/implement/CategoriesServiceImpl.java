package ceb.service.implement;

import java.util.List;

import org.springframework.stereotype.Service;

import ceb.domain.model.Categories;
import ceb.exception.CategoryNameRequiredException;
import ceb.exception.CategoryNotFoundException;
import ceb.repository.CategoriesRepository;
import ceb.service.service.CategoriesService;

@Service
public class CategoriesServiceImpl implements CategoriesService {

    private final CategoriesRepository categoriesRepository;

    public CategoriesServiceImpl(CategoriesRepository categoriesRepository) {
        this.categoriesRepository = categoriesRepository;
    }

    @Override
    public List<Categories> findAll() {
        return categoriesRepository.findAll();
    }

    @Override
    public Categories findById(int id) {
        Categories category = categoriesRepository.findById(id);
        if (category == null) {
            throw new CategoryNotFoundException(id);
        }
        return category;
    }

    @Override
    public Categories create(Categories category) {
        validateCategory(category);
        categoriesRepository.save(category);
        return category;
    }

    @Override
    public Categories update(int id, Categories category) {
        findById(id);
        validateCategory(category);
        category.setCategoryId(id);
        categoriesRepository.save(category);
        return category;
    }

    @Override
    public void delete(int id) {
        findById(id);
        categoriesRepository.delete(id);
    }

    private void validateCategory(Categories category) {
        if (category == null || category.getCategoryName() == null || category.getCategoryName().isBlank()) {
            throw new CategoryNameRequiredException();
        }
    }
}
