package com.easyeats.product.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.easyeats.product.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductIdentifier(String productIdentifier);

    boolean existsByProductIdentifier(String productIdentifier);
    
}
