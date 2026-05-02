package com.dailycodework.buynowdotcom.repository;

import com.dailycodework.buynowdotcom.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByNameAndBrand(String name, String brand);

    @Query("SELECT p FROM Product p WHERE LOWER(p.category.name) LIKE LOWER(CONCAT('%', :category, '%'))")
    List<Product> findByCategoryName(String category);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByName(String name);

    @Query("SELECT p FROM Product p WHERE LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%'))")
    List<Product> findByBrand(String brand);

    @Query("SELECT p FROM Product p WHERE LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%')) AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByBrandAndName(String brand, String name);

    @Query("SELECT p FROM Product p WHERE LOWER(p.category.name) LIKE LOWER(CONCAT('%', :category, '%')) AND LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%'))")
    List<Product> findByCategoryNameAndBrand(String category, String brand);
}
