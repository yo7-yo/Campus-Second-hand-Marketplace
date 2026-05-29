package com.example.secondhand.controller;

import com.example.secondhand.entity.Product;
import com.example.secondhand.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "搜索/查询商品")
    @GetMapping
    public List<Product> list(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer categoryId) {
        return productService.search(keyword, categoryId);
    }

    @Operation(summary = "查询商品详情")
    @GetMapping("/{id}")
    public Product detail(@PathVariable Integer id) {
        return productService.findById(id);
    }

    @Operation(summary = "发布商品（含图片上传）")
    @PostMapping
    public Product publish(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("ownerId") Integer ownerId,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {
        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setOwnerId(ownerId);
        product.setCategoryId(categoryId);
        product.setLocation(location);
        product.setStatus(status);
        return productService.publish(product, image);
    }

    @Operation(summary = "修改商品状态")
    @PutMapping("/{id}/status")
    public void updateStatus(@PathVariable Integer id, @RequestParam String status) {
        productService.updateStatus(id, status);
    }

    @Operation(summary = "编辑商品信息")
    @PutMapping("/{id}")
    public Product update(
            @PathVariable Integer id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {
        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategoryId(categoryId);
        product.setLocation(location);
        return productService.update(id, product, image);
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        productService.delete(id);
    }

    @Operation(summary = "查询用户发布的商品")
    @GetMapping("/owner/{ownerId}")
    public List<Product> findByOwner(@PathVariable Integer ownerId) {
        return productService.findByOwner(ownerId);
    }
}
