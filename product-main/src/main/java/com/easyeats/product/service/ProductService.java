package com.easyeats.product.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.easyeats.product.dto.ProductDto;
import com.easyeats.product.model.Category;
import com.easyeats.product.model.CategoryStatus;
import com.easyeats.product.model.Product;
import com.easyeats.product.model.ProductStatus;
import com.easyeats.product.repository.CategoryRepository;
import com.easyeats.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductDto create(ProductDto productDto) {
        Category category = getActiveCategory(productDto.getCategoriaId());
        if (productRepository.existsByProductIdentifierAndCategoryId(
                productDto.getProductIdentifier(), category.getId())) {
            throw conflict(productDto.getProductIdentifier(), category);
        }
        Product product = Product.convert(productDto);
        product.setCategory(category);
        return ProductDto.convert(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findAll(Long categoriaId) {
        List<Product> products = categoriaId == null
                ? productRepository.findAll()
                : productRepository.findByCategoryId(categoriaId);
        return products.stream()
                .map(ProductDto::convert)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDto findById(long id) {
        return ProductDto.convert(getProduct(id));
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findByProductIdentifier(String productIdentifier) {
        List<Product> products = productRepository.findByProductIdentifier(productIdentifier);
        if (products.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Produto não encontrado: " + productIdentifier);
        }
        return products.stream()
                .map(ProductDto::convert)
                .toList();
    }

    @Transactional
    public ProductDto update(long id, ProductDto productDto) {
        Product product = getProduct(id);

        Category category = product.getCategory();
        boolean categoryChanged = !category.getId().equals(productDto.getCategoriaId());
        if (categoryChanged) {
            category = getActiveCategory(productDto.getCategoriaId());
        }

        boolean identifierChanged = !product.getProductIdentifier().equals(productDto.getProductIdentifier());
        if ((identifierChanged || categoryChanged)
                && productRepository.existsByProductIdentifierAndCategoryId(
                        productDto.getProductIdentifier(), category.getId())) {
            throw conflict(productDto.getProductIdentifier(), category);
        }

        product.setProductIdentifier(productDto.getProductIdentifier());
        product.setCategory(category);
        product.setNome(productDto.getNome());
        product.setDescricao(productDto.getDescricao());
        product.setPreco(productDto.getPreco());
        if (productDto.getStatus() != null) {
            product.setStatus(productDto.getStatus());
        }
        return ProductDto.convert(productRepository.save(product));
    }

    @Transactional
    public void inactivate(long id) {
        Product product = getProduct(id);
        product.setStatus(ProductStatus.INATIVO);
        productRepository.save(product);
    }

    private Product getProduct(long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Produto não encontrado: " + id));
    }

    private Category getActiveCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoria não encontrada: " + categoryId));
        if (category.getStatus() != CategoryStatus.ATIVO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A categoria " + category.getNome() + " está inativa");
        }
        return category;
    }

    private ResponseStatusException conflict(String productIdentifier, Category category) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "Já existe o produto " + productIdentifier + " para a categoria " + category.getNome());
    }
}