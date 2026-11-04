package com.easyeats.product.dto;

import java.time.LocalDateTime;

import com.easyeats.product.model.Product;
import com.easyeats.product.model.ProductStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Product", description = "Produto disponível para compra")
public class ProductDto {

    @Schema(description = "Identificador do produto", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Schema(description = "Identificador funcional do produto", example = "NOTEBOOK-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productIdentifier;

    @NotBlank
    @Schema(description = "Nome do produto", example = "Notebook", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @NotBlank
    @Schema(description = "Descrição do produto", example = "Notebook 15 polegadas", requiredMode = Schema.RequiredMode.REQUIRED)
    private String descricao;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Preço do produto", example = "3500.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Float preco;

    @Schema(description = "Status do produto. Se não for informado, assume ATIVO", example = "ATIVO")
    private ProductStatus status;

    @Schema(description = "Data de criação do produto", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime criadoEm;

    @Schema(description = "Data da última atualização do produto", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime atualizadoEm;

    public static ProductDto convert(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setProductIdentifier(product.getProductIdentifier());
        productDto.setNome(product.getNome());
        productDto.setDescricao(product.getDescricao());
        productDto.setPreco(product.getPreco());
        productDto.setStatus(product.getStatus());
        productDto.setCriadoEm(product.getCriadoEm());
        productDto.setAtualizadoEm(product.getAtualizadoEm());
        return productDto;
    }
}