package com.example.letsplay.service;

import com.example.letsplay.dto.ProductRequest;
import com.example.letsplay.exception.ResourceNotFoundException;
import com.example.letsplay.model.Product;
import com.example.letsplay.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Product getProductById(String id) {
        Optional<Product> productOpt = productRepository.findById(id);
        return productOpt.orElseThrow(() ->
                new ResourceNotFoundException("Product not found with id " + id));
    }
    
    public Product createProduct(ProductRequest request) {
        Product product = new Product(request.getName(), request.getDescription(), request.getPrice());
        return productRepository.save(product);
    }
    
    public Product updateProduct(String id, ProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        return productRepository.save(existing);
    }
    
    public void deleteProduct(String id) {
        Product prod = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        productRepository.delete(prod);
    }
}
