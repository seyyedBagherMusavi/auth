package com.example.auth.category.service;

import com.example.auth.category.dto.CategoryDto;
import com.example.auth.category.entity.Category;
import com.example.auth.category.repository.CategoryRepository;
import com.example.auth.common.exception.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDto.Response create(CategoryDto.CreateRequest request) {
        Category category = new Category(request.name());
        Category savedCategory = categoryRepository.save(category);
        return CategoryDto.Response.from(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto.Response> findAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto.Response::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Category findEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id=" + id));
    }

    @Transactional(readOnly = true)
    public CategoryDto.Response findById(Long id) {
        return CategoryDto.Response.from(findEntityById(id));
    }

    public CategoryDto.Response update(Long id, CategoryDto.UpdateRequest request) {
        Category category = findEntityById(id);
        category.updateName(request.name());
        return CategoryDto.Response.from(category);
    }

    public void delete(Long id) {
        Category category = findEntityById(id);
        categoryRepository.delete(category);
    }
}
