package com.example.secondhand.controller;

import com.example.secondhand.entity.Category;
import com.example.secondhand.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "查询分类列表")
    @GetMapping
    public List<Category> list() {
        return categoryService.findAll();
    }
}
