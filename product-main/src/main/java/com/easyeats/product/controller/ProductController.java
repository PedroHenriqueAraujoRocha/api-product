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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.easyeats.product.dto.ProductDto;
import com.easyeats.product.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(
        summary = "Cadastrar produto",
        description = "Cadastra um novo produto no sistema",
        responses = {
            @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados do produto inválidos")
        }
    )
    public ResponseEntity<ProductDto> create(@RequestBody @Valid ProductDto productDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(productDto));
    }

    @GetMapping
    @Operation(
        summary = "Listar produtos",
        description = "Retorna a lista de produtos cadastrados. Use categoriaId para filtrar por categoria",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
        }
    )
    public ResponseEntity<List<ProductDto>> findAll(
            @RequestParam(required = false) Long categoriaId) {
        return ResponseEntity.ok(productService.findAll(categoriaId));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar produto por ID",
        description = "Retorna os dados de um produto específico através do seu ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
        }
    )
    public ResponseEntity<ProductDto> findById(@PathVariable long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping("/identifier/{productIdentifier}")
    @Operation(
        summary = "Buscar produtos por plano",
        description = "Retorna os produtos de um plano (BASIC, PLUS ou PREMIUM), um por categoria",
        responses = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
        }
    )
    public ResponseEntity<List<ProductDto>> findByProductIdentifier(
            @PathVariable String productIdentifier) {
        return ResponseEntity.ok(
                productService.findByProductIdentifier(productIdentifier));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar produto",
        description = "Atualiza os dados de um produto existente através do seu ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados do produto inválidos"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
        }
    )
    public ResponseEntity<ProductDto> update(
            @PathVariable long id,
            @RequestBody @Valid ProductDto productDto) {
        return ResponseEntity.ok(productService.update(id, productDto));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Inativar produto",
        description = "Inativa um produto pelo seu ID, mantendo seu registro no banco de dados",
        responses = {
            @ApiResponse(responseCode = "204", description = "Produto inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
        }
    )
    public ResponseEntity<Void> inactivate(@PathVariable long id) {
        productService.inactivate(id);
        return ResponseEntity.noContent().build();
    }
}
