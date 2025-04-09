package com.example.letsplay;


import com.example.letsplay.controller.ProductController;
import com.example.letsplay.exception.ResourceNotFoundException;
import com.example.letsplay.model.Product;
import com.example.letsplay.service.JwtUtil;
import com.example.letsplay.service.ProductService;
import com.example.letsplay.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


@WithMockUser
@AutoConfigureMockMvc(addFilters=false)
@WebMvcTest(ProductController.class)
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private ProductService productService;
    
    @MockitoBean
    private JwtUtil jwtUtil;
    
    @MockitoBean(name = "userService")
    UserService userService;
    
    
    @Test
    void testGetAllProducts_emptyList() throws Exception {
        // Arrange: Mock the service to return an empty list
        when(productService.getAllProducts()).thenReturn(emptyList());
        
        // Act & Assert: call GET /api/products and expect 200 OK and "[]"
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json("[]"));
    }
    
    @Test
    void testGetAllProducts_withProducts() throws Exception {
        // Arrange: create some dummy products
        Product p1 = new Product("Guitar", "Acoustic guitar", 99.99);
        Product p2 = new Product("Piano", "Electric piano", 199.99);
        List<Product> products = Arrays.asList(p1, p2);
        when(productService.getAllProducts()).thenReturn(products);
        
        System.out.println(p1.getName());
        
        // Act & Assert: perform GET and expect JSON array with 2 elements
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                // Use JSONPath or stringify expected JSON. For simplicity:
                .andExpect(jsonPath("$[0].name").value("Guitar"))
                .andExpect(jsonPath("$", hasSize(2)));
    }
    
    @Test
    void testGetProductById_found() throws Exception {
        // Arrange: dummy product and mock service
        Product prod = new Product("Guitar", "Acoustic guitar", 99.99);
        prod.setId("abc123");  // set an ID (simulating a product retrieved from DB)
        when(productService.getProductById("abc123")).thenReturn(prod);
        
        // Act & Assert: GET /api/products/abc123 returns the product
        mockMvc.perform(get("/api/products/{id}", "abc123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value("abc123"))
                .andExpect(jsonPath("$.name").value("Guitar"))
                .andExpect(jsonPath("$.price").value(99.99));
    }
    
    @Test
    void testGetProductById_notFound() throws Exception {
        // Arrange: have the service throw ResourceNotFound when called with unknown ID
        String missingId = "doesNotExist";
        doThrow(new ResourceNotFoundException("Product not found"))
                .when(productService).getProductById(missingId);
        
        // Act & Assert: GET /api/products/doesNotExist should yield 404
        mockMvc.perform(get("/api/products/{id}", missingId))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testCreateProduct_success() throws Exception {
        // Arrange: prepare a ProductRequest JSON and a Product to return
        String newProductJson = "{ \"name\": \"Violin\", \"description\": \"Classic violin\", \"price\": 149.99 }";
        Product created = new Product("Violin", "Classic violin", 149.99);
        created.setId("newId123");
        // Mock the service createProduct method
        when(productService.createProduct(any())).thenReturn(created);
        
        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newProductJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))  // Check if Location header is set
                .andExpect(jsonPath("$.id").value("newId123"))
                .andExpect(jsonPath("$.name").value("Violin"))
                .andExpect(jsonPath("$.description").value("Classic violin"));
    }
    
    @Test
    void testCreateProduct_validationError() throws Exception {
        // Arrange: JSON with missing name and price
        String invalidJson = "{ \"description\": \"No name and price\" }";
        
        // Act & Assert: expect Bad Request due to validation errors
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testUpdateProduct_success() throws Exception {
        String id = "prod123";
        String updateJson = "{ \"name\": \"Guitar UPDATED\", \"description\": \"Acoustic guitar\", \"price\": 109.99 }";
        Product updated = new Product("Guitar UPDATED", "Acoustic guitar", 109.99);
        updated.setId(id);
        when(productService.updateProduct(eq(id), any())).thenReturn(updated);
        
        mockMvc.perform(put("/api/products/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Guitar UPDATED"))
                .andExpect(jsonPath("$.price").value(109.99));
    }
    
    @Test
    void testUpdateProduct_notFound() throws Exception {
        String id = "missing123";
        String updateJson = "{ \"name\": \"Anything\", \"price\": 9.99 }";
        // Service will throw ResourceNotFound for missing id
        doThrow(new ResourceNotFoundException("Product not found"))
                .when(productService).updateProduct(eq(id), any());
        
        mockMvc.perform(put("/api/products/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testDeleteProduct_success() throws Exception {
        String id = "prod123";
        
        mockMvc.perform(delete("/api/products/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void testDeleteProduct_notFound() throws Exception {
        String id = "missing123";
        doThrow(new ResourceNotFoundException("Product not found"))
                .when(productService).deleteProduct(id);
        
        mockMvc.perform(delete("/api/products/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNotFound());

    }
    
    
}
