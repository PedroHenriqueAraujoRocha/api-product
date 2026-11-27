
package com.easyeats.product.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.easyeats.product.dto.ProductDto;
import com.easyeats.product.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    // 1. Teste de cadastro de produto
    @Test
    @DisplayName("Deve cadastrar um produto")
    void shouldCreateProduct() {

        ProductDto productDto = org.mockito.Mockito.mock(ProductDto.class);

        when(productService.create(productDto))
                .thenReturn(productDto);

        ResponseEntity<ProductDto> response = productController.create(productDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(productDto, response.getBody());

        verify(productService).create(productDto);
    }

    // 2. Teste de listagem de produtos
    @Test
    @DisplayName("Deve listar todos os produtos")
    void shouldFindAllProducts() {

        ProductDto product1 = org.mockito.Mockito.mock(ProductDto.class);
        ProductDto product2 = org.mockito.Mockito.mock(ProductDto.class);

        List<ProductDto> products = List.of(product1, product2);

        when(productService.findAll(null)).thenReturn(products);

        ResponseEntity<List<ProductDto>> response = productController.findAll(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertSame(products, response.getBody());

        verify(productService).findAll(null);
    }

    @Test
    @DisplayName("Deve listar produtos filtrando por categoria")
    void shouldFindAllProductsByCategory() {

        List<ProductDto> products = List.of(org.mockito.Mockito.mock(ProductDto.class));

        when(productService.findAll(5L)).thenReturn(products);

        ResponseEntity<List<ProductDto>> response = productController.findAll(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(products, response.getBody());

        verify(productService).findAll(5L);
    }

    // 3. Teste de busca por ID
    @Test
    @DisplayName("Deve buscar produto por ID")
    void shouldFindProductById() {

        long productId = 1L;

        ProductDto productDto = org.mockito.Mockito.mock(ProductDto.class);

        when(productService.findById(productId))
                .thenReturn(productDto);

        ResponseEntity<ProductDto> response = productController.findById(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(productDto, response.getBody());

        verify(productService).findById(productId);
    }

    // 4. Teste de busca por identificador
    @Test
    @DisplayName("Deve buscar produto pelo identificador")
    void shouldFindProductByProductIdentifier() {

        String identifier = "PLUS";

        List<ProductDto> products = List.of(org.mockito.Mockito.mock(ProductDto.class));

        when(productService.findByProductIdentifier(identifier))
                .thenReturn(products);

        ResponseEntity<List<ProductDto>> response = productController.findByProductIdentifier(identifier);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(products, response.getBody());

        verify(productService).findByProductIdentifier(identifier);
    }

    // 5. Teste de atualização de produto
    @Test
    @DisplayName("Deve atualizar um produto")
    void shouldUpdateProduct() {

        long productId = 1L;

        ProductDto productDto = org.mockito.Mockito.mock(ProductDto.class);

        when(productService.update(productId, productDto))
                .thenReturn(productDto);

        ResponseEntity<ProductDto> response = productController.update(productId, productDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(productDto, response.getBody());

        verify(productService).update(productId, productDto);
    }

    // 6. Teste de inativação de produto
    @Test
    @DisplayName("Deve inativar um produto")
    void shouldInactivateProduct() {

        long productId = 1L;

        ResponseEntity<Void> response = productController.inactivate(productId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(null, response.getBody());

        verify(productService).inactivate(productId);
    }

    @AfterEach
    void showTestResult(TestInfo testInfo) {
        System.out.println("--------------------------------");
        System.out.println("Teste executado: " + testInfo.getDisplayName());
        System.out.println("Execução finalizada!");
        System.out.println("--------------------------------");
    }
}
