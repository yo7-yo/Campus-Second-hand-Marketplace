package com.example.secondhand.service;

import com.example.secondhand.entity.Product;
import com.example.secondhand.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductMapper productMapper;
    private static final String UPLOAD_DIR = "uploads/products/";

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public List<Product> search(String keyword, Integer categoryId) {
        return productMapper.search(keyword, categoryId);
    }

    public Product findById(Integer id) {
        return productMapper.findById(id);
    }

    public String saveImage(MultipartFile image) throws IOException {
        // 使用项目根目录下的 uploads/products/ 目录
        String basePath = System.getProperty("user.dir");
        File dir = new File(basePath, UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String ext = "";
        String originalName = image.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + ext;
        image.transferTo(new File(dir, fileName));
        return fileName;
    }

    public Product publish(Product product, MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            product.setImagePath(saveImage(image));
        }
        if (product.getStatus() == null || product.getStatus().isBlank()) {
            product.setStatus("available");
        }
        productMapper.insert(product);
        return product;
    }

    public void updateStatus(Integer id, String status) {
        productMapper.updateStatus(id, status);
    }

    public Product update(Integer id, Product product, MultipartFile image) throws IOException {
        Product existing = productMapper.findById(id);
        existing.setTitle(product.getTitle());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setCategoryId(product.getCategoryId());
        existing.setLocation(product.getLocation());
        if (image != null && !image.isEmpty()) {
            existing.setImagePath(saveImage(image));
        }
        productMapper.update(existing);
        return existing;
    }

    public void delete(Integer id) {
        productMapper.delete(id);
    }

    public List<Product> findByOwner(Integer ownerId) {
        return productMapper.findByOwnerId(ownerId);
    }
}
