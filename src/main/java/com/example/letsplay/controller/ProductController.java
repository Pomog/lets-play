package com.example.letsplay.controller;


import com.example.letsplay.model.Product;
import com.example.letsplay.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        System.out.println("getAllProductsgetAllProductsgetAllProductsgetAllProductsgetAllProductsgetAllProducts");
        System.out.println(p13.getName());
        return productService.getAllProducts();
        
    }
    
}
