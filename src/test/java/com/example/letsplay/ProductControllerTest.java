package com.example.letsplay;


import com.example.letsplay.controller.ProductController;
import com.example.letsplay.model.Product;
import com.example.letsplay.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(ProductController.class)
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private ProductService productService;
    
    
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
    
    
}
