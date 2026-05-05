package com.dailycodework.buynowdotcom.controller;

import com.dailycodework.buynowdotcom.dto.ProductDto;
import com.dailycodework.buynowdotcom.model.Product;
import com.dailycodework.buynowdotcom.request.AddProductRequest;
import com.dailycodework.buynowdotcom.request.UpdateProductRequest;
import com.dailycodework.buynowdotcom.response.ApiResponse;
import com.dailycodework.buynowdotcom.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final IProductService productService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllProducts() {
        List<ProductDto> products = productService.getAllProducts().stream()
                .map(p -> modelMapper.map(p, ProductDto.class)).toList();
        return ResponseEntity.ok(new ApiResponse("Success", products));
    }

    @GetMapping("/distinct/products")
    public ResponseEntity<ApiResponse> getDistinctProducts() {
        List<ProductDto> products = productService.getDistinctProductsByName().stream()
                .map(p -> modelMapper.map(p, ProductDto.class)).toList();
        return ResponseEntity.ok(new ApiResponse("Success", products));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(new ApiResponse("Success", modelMapper.map(product, ProductDto.class)));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProduct(@RequestBody AddProductRequest request) {
        Product product = productService.addProduct(request);
        return ResponseEntity.ok(new ApiResponse("Product added successfully", modelMapper.map(product, ProductDto.class)));
    }

    @PutMapping("/{productId}/update")
    public ResponseEntity<ApiResponse> updateProduct(@RequestBody UpdateProductRequest request, @PathVariable Long productId) {
        Product product = productService.updateProduct(request, productId);
        return ResponseEntity.ok(new ApiResponse("Product updated successfully", modelMapper.map(product, ProductDto.class)));
    }

    @DeleteMapping("/{productId}/delete")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long productId) {
        productService.deleteProductById(productId);
        return ResponseEntity.ok(new ApiResponse("Product deleted successfully", null));
    }

    @GetMapping("/by/brand-and-name")
    public ResponseEntity<ApiResponse> getProductByBrandAndName(@RequestParam String brand, @RequestParam String name) {
        List<ProductDto> products = productService.getProductByBrandAndName(brand, name).stream()
                .map(p -> modelMapper.map(p, ProductDto.class)).toList();
        return ResponseEntity.ok(new ApiResponse("Success", products));
    }

    @GetMapping("/by/category-and-brand")
    public ResponseEntity<ApiResponse> getProductByCategoryAndBrand(@RequestParam String category, @RequestParam String brand) {
        List<ProductDto> products = productService.getProductByCategoryAndBrand(category, brand).stream()
                .map(p -> modelMapper.map(p, ProductDto.class)).toList();
        return ResponseEntity.ok(new ApiResponse("Success", products));
    }

    @GetMapping("/by/name")
    public ResponseEntity<ApiResponse> getProductByName(@RequestParam String name) {
        List<ProductDto> products = productService.getProductByName(name).stream()
                .map(p -> modelMapper.map(p, ProductDto.class)).toList();
        return ResponseEntity.ok(new ApiResponse("Success", products));
    }

    @GetMapping("/by/brand")
    public ResponseEntity<ApiResponse> getProductByBrand(@RequestParam String brand) {
        List<ProductDto> products = productService.getProductByBrand(brand).stream()
                .map(p -> modelMapper.map(p, ProductDto.class)).toList();
        return ResponseEntity.ok(new ApiResponse("Success", products));
    }

    @GetMapping("/by/category")
    public ResponseEntity<ApiResponse> getProductByCategory(@RequestParam String category) {
        List<ProductDto> products = productService.getProductByCategory(category).stream()
                .map(p -> modelMapper.map(p, ProductDto.class)).toList();
        return ResponseEntity.ok(new ApiResponse("Success", products));
    }
}