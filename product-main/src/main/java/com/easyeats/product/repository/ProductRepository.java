package com.easyeats.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.easyeats.product.model.Product;
import com.easyeats.product.model.ProductStatus;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByProductIdentifier(String productIdentifier);

    List<Product> findByCategoryId(Long categoryId);

    boolean existsByProductIdentifierAndCategoryId(String productIdentifier, Long categoryId);

    boolean existsByCategoryIdAndStatus(Long categoryId, ProductStatus status);
}