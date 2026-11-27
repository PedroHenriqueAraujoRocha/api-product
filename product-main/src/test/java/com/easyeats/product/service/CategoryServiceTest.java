package com.easyeats.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.easyeats.product.dto.CategoryDto;
import com.easyeats.product.model.Category;
import com.easyeats.product.model.CategoryStatus;
import com.easyeats.product.model.ProductStatus;
import com.easyeats.product.repository.CategoryRepository;
import com.easyeats.product.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryDto buildDto(String nome, CategoryStatus status) {
        CategoryDto dto = new CategoryDto();
        dto.setNome(nome);
        dto.setDescricao("Descricao " + nome);
        dto.setStatus(status);
        return dto;
    }

    private Category buildCategory(long id, String nome, CategoryStatus status) {
        Category category = new Category();
        category.setId(id);
        category.setNome(nome);
        category.setDescricao("Descricao " + nome);
        category.setStatus(status);
        return category;
    }

    // ---------- create ----------

    @Test
    @DisplayName("create: salva a categoria e assume ATIVO quando status nao e informado")
    void createShouldSaveWithDefaultStatus() {
        when(categoryRepository.existsByNomeIgnoreCase("Pizzaria")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CategoryDto result = categoryService.create(buildDto("Pizzaria", null));

        assertEquals(1L, result.getId());
        assertEquals("Pizzaria", result.getNome());
        assertEquals(CategoryStatus.ATIVO, result.getStatus());
    }

    @Test
    @DisplayName("create: respeita o status informado")
    void createShouldKeepInformedStatus() {
        when(categoryRepository.existsByNomeIgnoreCase("Sushi")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryDto result = categoryService.create(buildDto("Sushi", CategoryStatus.INATIVO));

        assertEquals(CategoryStatus.INATIVO, result.getStatus());
    }

    @Test
    @DisplayName("create: lanca 409 quando o nome ja existe")
    void createShouldThrowConflictWhenNameExists() {
        when(categoryRepository.existsByNomeIgnoreCase("Pizzaria")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categoryService.create(buildDto("Pizzaria", null)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(categoryRepository, never()).save(any());
    }

    // ---------- findAll ----------

    @Test
    @DisplayName("findAll: converte todas as categorias para DTO")
    void findAllShouldReturnDtos() {
        when(categoryRepository.findAll()).thenReturn(List.of(
                buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO),
                buildCategory(2L, "Sushi", CategoryStatus.INATIVO)));

        List<CategoryDto> result = categoryService.findAll();

        assertEquals(2, result.size());
        assertEquals("Pizzaria", result.get(0).getNome());
        assertEquals("Sushi", result.get(1).getNome());
    }

    @Test
    @DisplayName("findAll: retorna lista vazia quando nao ha categorias")
    void findAllShouldReturnEmptyList() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        assertEquals(0, categoryService.findAll().size());
    }

    // ---------- findById ----------

    @Test
    @DisplayName("findById: retorna a categoria encontrada")
    void findByIdShouldReturnCategory() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO)));

        CategoryDto result = categoryService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Pizzaria", result.getNome());
    }

    @Test
    @DisplayName("findById: lanca 404 quando nao encontra")
    void findByIdShouldThrowNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categoryService.findById(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ---------- update ----------

    @Test
    @DisplayName("update: atualiza os campos e o status quando informado")
    void updateShouldChangeFields() {
        Category existing = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNomeIgnoreCase("Pizzaria Gourmet")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryDto result = categoryService.update(1L, buildDto("Pizzaria Gourmet", CategoryStatus.INATIVO));

        assertEquals("Pizzaria Gourmet", result.getNome());
        assertEquals(CategoryStatus.INATIVO, result.getStatus());
    }

    @Test
    @DisplayName("update: mantem o status atual quando o DTO nao informa status")
    void updateShouldKeepStatusWhenNull() {
        Category existing = buildCategory(1L, "Pizzaria", CategoryStatus.INATIVO);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryDto result = categoryService.update(1L, buildDto("Pizzaria", null));

        assertEquals(CategoryStatus.INATIVO, result.getStatus());
        verify(categoryRepository, never()).existsByNomeIgnoreCase(any());
    }

    @Test
    @DisplayName("update: nao checa duplicidade quando so muda maiusculas/minusculas do proprio nome")
    void updateShouldAllowCaseOnlyChangeOfOwnName() {
        Category existing = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryDto result = categoryService.update(1L, buildDto("PIZZARIA", null));

        assertEquals("PIZZARIA", result.getNome());
        verify(categoryRepository, never()).existsByNomeIgnoreCase(any());
    }

    @Test
    @DisplayName("update: lanca 409 quando o novo nome ja pertence a outra categoria")
    void updateShouldThrowConflictWhenNewNameExists() {
        Category existing = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNomeIgnoreCase("Sushi")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categoryService.update(1L, buildDto("Sushi", null)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: lanca 404 quando a categoria nao existe")
    void updateShouldThrowNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categoryService.update(99L, buildDto("Pizzaria", null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(categoryRepository, never()).save(any());
    }

    // ---------- inactivate ----------

    @Test
    @DisplayName("inactivate: marca a categoria como INATIVO quando nao ha produtos ativos")
    void inactivateShouldSetStatusInativo() {
        Category existing = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsByCategoryIdAndStatus(1L, ProductStatus.ATIVO)).thenReturn(false);

        categoryService.inactivate(1L);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertEquals(CategoryStatus.INATIVO, captor.getValue().getStatus());
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("inactivate: lanca 409 quando existem produtos ativos na categoria")
    void inactivateShouldThrowConflictWhenActiveProductsExist() {
        Category existing = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsByCategoryIdAndStatus(1L, ProductStatus.ATIVO)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categoryService.inactivate(1L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("inactivate: lanca 404 quando a categoria nao existe")
    void inactivateShouldThrowNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categoryService.inactivate(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(categoryRepository, never()).save(any());
    }
}