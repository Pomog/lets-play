package com.example.letsplay.controller;


import com.example.letsplay.dto.ProductRequest;
import com.example.letsplay.model.Product;
import com.example.letsplay.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    // GET /api/products - list all products
    @GetMapping
    public List<Product> getAllProducts() {
        Product p13 = new Product("asdad", "asdasd", 44.44);
        return productService.getAllProducts();
    }
    
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) {
        return productService.getProductById(id);
    }
    
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        Product created = productService.createProduct(productRequest);
        String newProductId = created.getId();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/products/" + newProductId);
        return ResponseEntity.status(201).headers(headers).body(created);
    }
    
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable String id, @Valid @RequestBody ProductRequest productRequest) {
        return productService.updateProduct(id, productRequest);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    
}
