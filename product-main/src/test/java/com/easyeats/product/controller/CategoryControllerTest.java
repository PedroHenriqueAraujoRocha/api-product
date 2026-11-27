package com.easyeats.product.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.easyeats.product.dto.CategoryDto;
import com.easyeats.product.service.CategoryService;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    @DisplayName("Deve cadastrar uma categoria")
    void shouldCreateCategory() {
        CategoryDto dto = mock(CategoryDto.class);
        when(categoryService.create(dto)).thenReturn(dto);

        ResponseEntity<CategoryDto> response = categoryController.create(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
        verify(categoryService).create(dto);
    }

    @Test
    @DisplayName("Deve listar todas as categorias")
    void shouldFindAllCategories() {
        List<CategoryDto> categories = List.of(mock(CategoryDto.class), mock(CategoryDto.class));
        when(categoryService.findAll()).thenReturn(categories);

        ResponseEntity<List<CategoryDto>> response = categoryController.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(categories, response.getBody());
        verify(categoryService).findAll();
    }

    @Test
    @DisplayName("Deve buscar categoria por ID")
    void shouldFindCategoryById() {
        CategoryDto dto = mock(CategoryDto.class);
        when(categoryService.findById(1L)).thenReturn(dto);

        ResponseEntity<CategoryDto> response = categoryController.findById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
        verify(categoryService).findById(1L);
    }

    @Test
    @DisplayName("Deve atualizar uma categoria")
    void shouldUpdateCategory() {
        CategoryDto dto = mock(CategoryDto.class);
        when(categoryService.update(1L, dto)).thenReturn(dto);

        ResponseEntity<CategoryDto> response = categoryController.update(1L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
        verify(categoryService).update(1L, dto);
    }

    @Test
    @DisplayName("Deve inativar uma categoria")
    void shouldInactivateCategory() {
        ResponseEntity<Void> response = categoryController.inactivate(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(categoryService).inactivate(1L);
    }
}