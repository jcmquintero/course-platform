package com.courses.platform.category;

import com.courses.platform.category.dto.CategoryResponse;
import com.courses.platform.category.dto.CreateCategoryRequest;
import com.courses.platform.category.dto.UpdateCategoryRequest;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(
            @Valid @RequestBody CreateCategoryRequest request) {

        return categoryService.create(request);
    }

    @GetMapping("/{id}")
    public CategoryResponse findById(@PathVariable UUID id) {
        return categoryService.findById(id);
    }

    @GetMapping
    public Page<CategoryResponse> findAll(Pageable pageable) {
        return categoryService.findAll(pageable);
    }

    @PostMapping("/{id}/archive")
    public CategoryResponse archive(@PathVariable UUID id) {
        return categoryService.archive(id);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {

        return categoryService.update(id, request);
    }
}