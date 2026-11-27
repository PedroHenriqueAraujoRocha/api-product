package com.easyeats.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.easyeats.product.model.Product;
import com.easyeats.product.model.ProductStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Product", description = "Plano do Easy Eats oferecido para um tipo de estabelecimento")
public class ProductDto {

    @Schema(description = "Identificador do produto", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Pattern(regexp = "BASIC|PLUS|PREMIUM", message = "deve ser BASIC, PLUS ou PREMIUM")
    @Schema(description = "Plano do produto", example = "PLUS", allowableValues = { "BASIC", "PLUS", "PREMIUM" },
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String productIdentifier;

    @NotBlank
    @Schema(description = "Nome do produto", example = "Plano Plus para Pizzarias", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @NotBlank
    @Schema(description = "Descrição do produto", example = "Cardápio digital e gestão de pedidos", requiredMode = Schema.RequiredMode.REQUIRED)
    private String descricao;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Preço do produto", example = "99.90", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal preco;

    @NotNull
    @Schema(description = "Identificador da categoria (tipo de estabelecimento)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoriaId;

    @Schema(description = "Nome da categoria", example = "Pizzaria", accessMode = Schema.AccessMode.READ_ONLY)
    private String categoriaNome;

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
        productDto.setCategoriaId(product.getCategory().getId());
        productDto.setCategoriaNome(product.getCategory().getNome());
        productDto.setStatus(product.getStatus());
        productDto.setCriadoEm(product.getCriadoEm());
        productDto.setAtualizadoEm(product.getAtualizadoEm());
        return productDto;
    }
}