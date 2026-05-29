package com.example.secondhand.service;

import com.example.secondhand.entity.Category;
import com.example.secondhand.mapper.CategoryMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> findAll() {
        return categoryMapper.findAll();
    }
}
