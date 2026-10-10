package com.easyeats.product.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.easyeats.product.dto.ProductDto;
import com.easyeats.product.model.Product;
import com.easyeats.product.model.ProductStatus;
import com.easyeats.product.repository.ProductRepository;

import org.springframework.transaction.annotation.Transactional;import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ProductService {

    private final ProductRepository productRepository;

     @Transactional
    public ProductDto create(ProductDto productDto) {
        if (productRepository.existsByProductIdentifier(productDto.getProductIdentifier())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe um produto com o identificador " + productDto.getProductIdentifier());
        }
        Product product = Product.convert(productDto);
        return ProductDto.convert(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        return productRepository.findAll().stream()
                .map(ProductDto::convert)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDto findById(long id) {
        return ProductDto.convert(getProduct(id));
    }

    @Transactional(readOnly = true)
    public ProductDto findByProductIdentifier(String productIdentifier) {
        Product product = productRepository.findByProductIdentifier(productIdentifier)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Produto não encontrado: " + productIdentifier));
        return ProductDto.convert(product);
    }

    @Transactional
    public ProductDto update(long id, ProductDto productDto) {
        Product product = getProduct(id);

        boolean identifierChanged = !product.getProductIdentifier().equals(productDto.getProductIdentifier());
        if (identifierChanged && productRepository.existsByProductIdentifier(productDto.getProductIdentifier())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe um produto com o identificador " + productDto.getProductIdentifier());
        }

        product.setProductIdentifier(productDto.getProductIdentifier());
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
    
}
