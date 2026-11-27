package com.easyeats.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easyeats.product.dto.CategoryDto;
import com.easyeats.product.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Gerenciamento de categorias (tipos de estabelecimento)")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(
        summary = "Cadastrar categoria",
        description = "Cadastra uma nova categoria de estabelecimento",
        responses = {
            @ApiResponse(responseCode = "201", description = "Categoria cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da categoria inválidos"),
            @ApiResponse(responseCode = "409", description = "Já existe uma categoria com esse nome")
        }
    )
    public ResponseEntity<CategoryDto> create(@RequestBody @Valid CategoryDto categoryDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(categoryDto));
    }

    @GetMapping
    @Operation(
        summary = "Listar categorias",
        description = "Retorna a lista de todas as categorias cadastradas",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso")
        }
    )
    public ResponseEntity<List<CategoryDto>> findAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar categoria por ID",
        description = "Retorna os dados de uma categoria através do seu ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
        }
    )
    public ResponseEntity<CategoryDto> findById(@PathVariable long id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar categoria",
        description = "Atualiza os dados de uma categoria existente",
        responses = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da categoria inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "409", description = "Já existe uma categoria com esse nome")
        }
    )
    public ResponseEntity<CategoryDto> update(
            @PathVariable long id,
            @RequestBody @Valid CategoryDto categoryDto) {
        return ResponseEntity.ok(categoryService.update(id, categoryDto));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Inativar categoria",
        description = "Inativa uma categoria, mantendo seu registro. Não é permitido se houver produtos ativos nela",
        responses = {
            @ApiResponse(responseCode = "204", description = "Categoria inativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "409", description = "Existem produtos ativos vinculados")
        }
    )
    public ResponseEntity<Void> inactivate(@PathVariable long id) {
        categoryService.inactivate(id);
        return ResponseEntity.noContent().build();
    }
}