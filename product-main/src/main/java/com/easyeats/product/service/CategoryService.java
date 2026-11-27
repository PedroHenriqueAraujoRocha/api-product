package com.easyeats.product.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.easyeats.product.dto.CategoryDto;
import com.easyeats.product.model.Category;
import com.easyeats.product.model.CategoryStatus;
import com.easyeats.product.model.ProductStatus;
import com.easyeats.product.repository.CategoryRepository;
import com.easyeats.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CategoryDto create(CategoryDto categoryDto) {
        if (categoryRepository.existsByNomeIgnoreCase(categoryDto.getNome())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe uma categoria com o nome " + categoryDto.getNome());
        }
        Category category = Category.convert(categoryDto);
        return CategoryDto.convert(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto::convert)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto findById(long id) {
        return CategoryDto.convert(getCategory(id));
    }

    @Transactional
    public CategoryDto update(long id, CategoryDto categoryDto) {
        Category category = getCategory(id);

        boolean nameChanged = !category.getNome().equalsIgnoreCase(categoryDto.getNome());
        if (nameChanged && categoryRepository.existsByNomeIgnoreCase(categoryDto.getNome())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe uma categoria com o nome " + categoryDto.getNome());
        }

        category.setNome(categoryDto.getNome());
        category.setDescricao(categoryDto.getDescricao());
        if (categoryDto.getStatus() != null) {
            category.setStatus(categoryDto.getStatus());
        }
        return CategoryDto.convert(categoryRepository.save(category));
    }

    @Transactional
    public void inactivate(long id) {
        Category category = getCategory(id);
        if (productRepository.existsByCategoryIdAndStatus(id, ProductStatus.ATIVO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível inativar a categoria: existem produtos ativos vinculados a ela");
        }
        category.setStatus(CategoryStatus.INATIVO);
        categoryRepository.save(category);
    }

    private Category getCategory(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoria não encontrada: " + id));
    }
}