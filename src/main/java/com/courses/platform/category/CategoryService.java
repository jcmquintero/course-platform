package com.courses.platform.category;

import com.courses.platform.category.dto.CategoryResponse;
import com.courses.platform.category.dto.CreateCategoryRequest;
import com.courses.platform.shared.ConflictException;
import com.courses.platform.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {

        if (categoryRepository.existsByName(request.name())) {
            throw new ConflictException(
                    "Category name already exists: " + request.name());
        }

        Category category = new Category(
                request.name(),
                request.description());

        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(UUID id) {
        return toResponse(getCategory(id));
    }

    @Transactional
    public CategoryResponse archive(UUID id) {
        Category category = getCategory(id);
        category.archive();

        return toResponse(category);
    }

    private Category getCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found: " + id));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive());
    }
}