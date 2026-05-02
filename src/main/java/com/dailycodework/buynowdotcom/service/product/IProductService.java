package com.dailycodework.buynowdotcom.service.product;

import com.dailycodework.buynowdotcom.model.Product;
import com.dailycodework.buynowdotcom.request.AddProductRequest;
import com.dailycodework.buynowdotcom.request.UpdateProductRequest;

import java.util.List;

public interface IProductService {
    Product addProduct(AddProductRequest request);
    Product getProductById(Long productId);
    Product updateProduct(UpdateProductRequest request, Long productId);
    void deleteProductById(Long productId);

    List<Product> getAllProducts();
    List<Product> getProductByCategory(String category);
    List<Product> getProductByName(String name);
    List<Product> getProductByBrand(String brand);
    List<Product> getProductByBrandAndName(String brand, String name);
    List<Product> getProductByCategoryAndBrand(String category, String brand);

}
