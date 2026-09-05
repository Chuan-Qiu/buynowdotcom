package com.dailycodework.buynowdotcom.controller;

import com.dailycodework.buynowdotcom.model.Category;
import com.dailycodework.buynowdotcom.response.ApiResponse;
import com.dailycodework.buynowdotcom.service.category.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final ICategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllCategories() {
        return ResponseEntity.ok(new ApiResponse("Success", categoryService.getAllCategories()));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllCategoriesList() {
        return ResponseEntity.ok(new ApiResponse("Success", categoryService.getAllCategories()));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addCategory(@RequestBody Category category) {
        Category saved = categoryService.addCategory(category);
        return ResponseEntity.ok(new ApiResponse("Category added", saved));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable Long categoryId) {
        return ResponseEntity.ok(new ApiResponse("Success", categoryService.findCategoryById(categoryId)));
    }

    @GetMapping("/by/name")
    public ResponseEntity<ApiResponse> getCategoryByName(@RequestParam String name) {
        return ResponseEntity.ok(new ApiResponse("Success", categoryService.findCategoryByName(name)));
    }

    @DeleteMapping("/{categoryId}/delete")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(new ApiResponse("Category deleted", null));
    }

    @PutMapping("/{categoryId}/update")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable Long categoryId, @RequestBody Category category) {
        Category updated = categoryService.updateCategoryByName(category, categoryId);
        return ResponseEntity.ok(new ApiResponse("Category updated", updated));
    }
}