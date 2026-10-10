package com.easyeats.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easyeats.product.dto.ProductDto;
import com.easyeats.product.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Gerenciamento de produtos")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Cadastra um novo produto")
    public ResponseEntity<ProductDto> create(@RequestBody @Valid ProductDto productDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(productDto));
    }

    @GetMapping
    @Operation(summary = "Lista todos os produtos")
    public ResponseEntity<List<ProductDto>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um produto pelo id")
    public ResponseEntity<ProductDto> findById(@PathVariable long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping("/identifier/{productIdentifier}")
    @Operation(summary = "Busca um produto pelo identificador funcional")
    public ResponseEntity<ProductDto> findByProductIdentifier(@PathVariable String productIdentifier) {
        return ResponseEntity.ok(productService.findByProductIdentifier(productIdentifier));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um produto")
    public ResponseEntity<ProductDto> update(@PathVariable long id,
                                             @RequestBody @Valid ProductDto productDto) {
        return ResponseEntity.ok(productService.update(id, productDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa um produto (não apaga do banco)")
    public ResponseEntity<Void> inactivate(@PathVariable long id) {
        productService.inactivate(id);
        return ResponseEntity.noContent().build();
    }
}
