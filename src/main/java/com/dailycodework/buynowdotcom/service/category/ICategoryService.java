package com.dailycodework.buynowdotcom.service.category;

import com.dailycodework.buynowdotcom.model.Category;

import java.util.List;

public interface ICategoryService {
    Category addCategory(Category category);
    Category updateCategoryByName(Category category, Long categoryId);
    void deleteCategory(Long categoryId);
    List<Category> getAllCategories();
    Category findCategoryByName(String name);
    Category findCategoryById(Long categoryId);
}