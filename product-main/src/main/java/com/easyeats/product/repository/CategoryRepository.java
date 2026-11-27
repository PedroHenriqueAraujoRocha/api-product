package com.easyeats.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.easyeats.product.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNomeIgnoreCase(String nome);
}