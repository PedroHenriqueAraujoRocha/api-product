package com.easyeats.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

import com.easyeats.product.dto.ProductDto;
import com.easyeats.product.model.Category;
import com.easyeats.product.model.CategoryStatus;
import com.easyeats.product.model.Product;
import com.easyeats.product.model.ProductStatus;
import com.easyeats.product.repository.CategoryRepository;
import com.easyeats.product.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Category buildCategory(long id, String nome, CategoryStatus status) {
        Category category = new Category();
        category.setId(id);
        category.setNome(nome);
        category.setStatus(status);
        return category;
    }

    private ProductDto buildDto(String identifier, long categoriaId, ProductStatus status) {
        ProductDto dto = new ProductDto();
        dto.setProductIdentifier(identifier);
        dto.setNome("Plano " + identifier);
        dto.setDescricao("Descricao " + identifier);
        dto.setPreco(new BigDecimal("99.90"));
        dto.setCategoriaId(categoriaId);
        dto.setStatus(status);
        return dto;
    }

    private Product buildProduct(long id, String identifier, Category category, ProductStatus status) {
        Product product = new Product();
        product.setId(id);
        product.setProductIdentifier(identifier);
        product.setNome("Plano " + identifier);
        product.setDescricao("Descricao " + identifier);
        product.setPreco(new BigDecimal("99.90"));
        product.setCategory(category);
        product.setStatus(status);
        product.setCriadoEm(LocalDateTime.now());
        product.setAtualizadoEm(LocalDateTime.now());
        return product;
    }

    // ---------- create ----------

    @Test
    @DisplayName("create: salva o produto e assume ATIVO quando status nao e informado")
    void createShouldSaveWithDefaultStatus() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(pizzaria));
        when(productRepository.existsByProductIdentifierAndCategoryId("PLUS", 1L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProductDto result = productService.create(buildDto("PLUS", 1L, null));

        assertEquals(10L, result.getId());
        assertEquals("PLUS", result.getProductIdentifier());
        assertEquals(new BigDecimal("99.90"), result.getPreco());
        assertEquals(1L, result.getCategoriaId());
        assertEquals("Pizzaria", result.getCategoriaNome());
        assertEquals(ProductStatus.ATIVO, result.getStatus());
    }

    @Test
    @DisplayName("create: respeita o status informado")
    void createShouldKeepInformedStatus() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(pizzaria));
        when(productRepository.existsByProductIdentifierAndCategoryId("BASIC", 1L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductDto result = productService.create(buildDto("BASIC", 1L, ProductStatus.INATIVO));

        assertEquals(ProductStatus.INATIVO, result.getStatus());
    }

    @Test
    @DisplayName("create: lanca 409 quando ja existe o mesmo plano na categoria")
    void createShouldThrowConflictWhenPlanExistsInCategory() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(pizzaria));
        when(productRepository.existsByProductIdentifierAndCategoryId("PLUS", 1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.create(buildDto("PLUS", 1L, null)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: lanca 404 quando a categoria nao existe")
    void createShouldThrowNotFoundWhenCategoryMissing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.create(buildDto("PLUS", 99L, null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: lanca 400 quando a categoria esta inativa")
    void createShouldThrowBadRequestWhenCategoryInactive() {
        Category inactive = buildCategory(1L, "Pizzaria", CategoryStatus.INATIVO);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(inactive));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.create(buildDto("PLUS", 1L, null)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    // ---------- findAll ----------

    @Test
    @DisplayName("findAll: sem filtro retorna todos os produtos como DTO")
    void findAllShouldReturnAllDtos() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        when(productRepository.findAll()).thenReturn(List.of(
                buildProduct(1L, "BASIC", pizzaria, ProductStatus.ATIVO),
                buildProduct(2L, "PLUS", pizzaria, ProductStatus.INATIVO)));

        List<ProductDto> result = productService.findAll(null);

        assertEquals(2, result.size());
        assertEquals("BASIC", result.get(0).getProductIdentifier());
        assertEquals("PLUS", result.get(1).getProductIdentifier());
        verify(productRepository, never()).findByCategoryId(any());
    }

    @Test
    @DisplayName("findAll: com categoriaId filtra pela categoria")
    void findAllShouldFilterByCategory() {
        Category sushi = buildCategory(2L, "Sushi", CategoryStatus.ATIVO);
        when(productRepository.findByCategoryId(2L)).thenReturn(List.of(
                buildProduct(3L, "PREMIUM", sushi, ProductStatus.ATIVO)));

        List<ProductDto> result = productService.findAll(2L);

        assertEquals(1, result.size());
        assertEquals("Sushi", result.get(0).getCategoriaNome());
        verify(productRepository, never()).findAll();
    }

    @Test
    @DisplayName("findAll: retorna lista vazia quando nao ha produtos")
    void findAllShouldReturnEmptyList() {
        when(productRepository.findAll()).thenReturn(List.of());

        assertEquals(0, productService.findAll(null).size());
    }

    // ---------- findById ----------

    @Test
    @DisplayName("findById: retorna o produto encontrado")
    void findByIdShouldReturnProduct() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(buildProduct(1L, "PLUS", pizzaria, ProductStatus.ATIVO)));

        ProductDto result = productService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("PLUS", result.getProductIdentifier());
        assertNotNull(result.getCriadoEm());
    }

    @Test
    @DisplayName("findById: lanca 404 quando nao encontra")
    void findByIdShouldThrowNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.findById(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ---------- findByProductIdentifier ----------

    @Test
    @DisplayName("findByProductIdentifier: retorna um produto por categoria")
    void findByIdentifierShouldReturnProducts() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        Category sushi = buildCategory(2L, "Sushi", CategoryStatus.ATIVO);
        when(productRepository.findByProductIdentifier("PLUS")).thenReturn(List.of(
                buildProduct(1L, "PLUS", pizzaria, ProductStatus.ATIVO),
                buildProduct(2L, "PLUS", sushi, ProductStatus.ATIVO)));

        List<ProductDto> result = productService.findByProductIdentifier("PLUS");

        assertEquals(2, result.size());
        assertEquals("Pizzaria", result.get(0).getCategoriaNome());
        assertEquals("Sushi", result.get(1).getCategoriaNome());
    }

    @Test
    @DisplayName("findByProductIdentifier: lanca 404 quando nao encontra")
    void findByIdentifierShouldThrowNotFound() {
        when(productRepository.findByProductIdentifier("PREMIUM")).thenReturn(List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.findByProductIdentifier("PREMIUM"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ---------- update ----------

    @Test
    @DisplayName("update: atualiza os campos e o status sem checar duplicidade quando plano e categoria nao mudam")
    void updateShouldChangeFields() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        Product existing = buildProduct(1L, "PLUS", pizzaria, ProductStatus.ATIVO);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductDto dto = buildDto("PLUS", 1L, ProductStatus.INATIVO);
        dto.setNome("Novo nome");
        dto.setDescricao("Nova descricao");
        dto.setPreco(new BigDecimal("129.90"));

        ProductDto result = productService.update(1L, dto);

        assertEquals("Novo nome", result.getNome());
        assertEquals("Nova descricao", result.getDescricao());
        assertEquals(new BigDecimal("129.90"), result.getPreco());
        assertEquals(ProductStatus.INATIVO, result.getStatus());
        verify(productRepository, never()).existsByProductIdentifierAndCategoryId(any(), any());
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    @DisplayName("update: mantem o status atual quando o DTO nao informa status")
    void updateShouldKeepStatusWhenNull() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        Product existing = buildProduct(1L, "PLUS", pizzaria, ProductStatus.INATIVO);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductDto result = productService.update(1L, buildDto("PLUS", 1L, null));

        assertEquals(ProductStatus.INATIVO, result.getStatus());
    }

    @Test
    @DisplayName("update: permite trocar para um plano livre na mesma categoria")
    void updateShouldAllowNewFreePlan() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        Product existing = buildProduct(1L, "BASIC", pizzaria, ProductStatus.ATIVO);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsByProductIdentifierAndCategoryId("PREMIUM", 1L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductDto result = productService.update(1L, buildDto("PREMIUM", 1L, null));

        assertEquals("PREMIUM", result.getProductIdentifier());
    }

    @Test
    @DisplayName("update: lanca 409 quando o novo plano ja existe na categoria")
    void updateShouldThrowConflictWhenPlanExistsInCategory() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        Product existing = buildProduct(1L, "BASIC", pizzaria, ProductStatus.ATIVO);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsByProductIdentifierAndCategoryId("PLUS", 1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.update(1L, buildDto("PLUS", 1L, null)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: permite mover o produto para outra categoria ativa")
    void updateShouldAllowMovingToAnotherCategory() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        Category sushi = buildCategory(2L, "Sushi", CategoryStatus.ATIVO);
        Product existing = buildProduct(1L, "PLUS", pizzaria, ProductStatus.ATIVO);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(sushi));
        when(productRepository.existsByProductIdentifierAndCategoryId("PLUS", 2L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductDto result = productService.update(1L, buildDto("PLUS", 2L, null));

        assertEquals(2L, result.getCategoriaId());
        assertEquals("Sushi", result.getCategoriaNome());
    }

    @Test
    @DisplayName("update: lanca 409 quando a categoria de destino ja tem o plano")
    void updateShouldThrowConflictWhenTargetCategoryHasPlan() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        Category sushi = buildCategory(2L, "Sushi", CategoryStatus.ATIVO);
        Product existing = buildProduct(1L, "PLUS", pizzaria, ProductStatus.ATIVO);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(sushi));
        when(productRepository.existsByProductIdentifierAndCategoryId("PLUS", 2L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.update(1L, buildDto("PLUS", 2L, null)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: lanca 400 ao mover para categoria inativa")
    void updateShouldThrowBadRequestWhenTargetCategoryInactive() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        Category sushi = buildCategory(2L, "Sushi", CategoryStatus.INATIVO);
        Product existing = buildProduct(1L, "PLUS", pizzaria, ProductStatus.ATIVO);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(sushi));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.update(1L, buildDto("PLUS", 2L, null)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: lanca 404 quando a categoria de destino nao existe")
    void updateShouldThrowNotFoundWhenTargetCategoryMissing() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        Product existing = buildProduct(1L, "PLUS", pizzaria, ProductStatus.ATIVO);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.update(1L, buildDto("PLUS", 99L, null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: lanca 404 quando o produto nao existe")
    void updateShouldThrowNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.update(99L, buildDto("PLUS", 1L, null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    // ---------- inactivate ----------

    @Test
    @DisplayName("inactivate: marca o produto como INATIVO sem apagar o registro")
    void inactivateShouldSetStatusInativo() {
        Category pizzaria = buildCategory(1L, "Pizzaria", CategoryStatus.ATIVO);
        Product existing = buildProduct(1L, "PLUS", pizzaria, ProductStatus.ATIVO);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));

        productService.inactivate(1L);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertEquals(ProductStatus.INATIVO, captor.getValue().getStatus());
        verify(productRepository, never()).delete(any());
    }

    @Test
    @DisplayName("inactivate: lanca 404 quando o produto nao existe")
    void inactivateShouldThrowNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.inactivate(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }
}