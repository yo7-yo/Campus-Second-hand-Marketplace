package com.example.secondhand.mapper;

import com.example.secondhand.entity.Category;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CategoryMapper {
    @Select("SELECT id, name FROM category ORDER BY id")
    List<Category> findAll();
}
