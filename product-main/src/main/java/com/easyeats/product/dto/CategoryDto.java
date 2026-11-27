package com.easyeats.product.dto;

import java.time.LocalDateTime;

import com.easyeats.product.model.Category;
import com.easyeats.product.model.CategoryStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Category", description = "Categoria (tipo de estabelecimento) atendida pelos planos")
public class CategoryDto {

    @Schema(description = "Identificador da categoria", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Schema(description = "Nome da categoria", example = "Pizzaria", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @Schema(description = "Descrição da categoria", example = "Pizzarias e restaurantes italianos")
    private String descricao;

    @Schema(description = "Status da categoria. Se não for informado, assume ATIVO", example = "ATIVO")
    private CategoryStatus status;

    @Schema(description = "Data de criação da categoria", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime criadoEm;

    @Schema(description = "Data da última atualização da categoria", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime atualizadoEm;

    public static CategoryDto convert(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setNome(category.getNome());
        dto.setDescricao(category.getDescricao());
        dto.setStatus(category.getStatus());
        dto.setCriadoEm(category.getCriadoEm());
        dto.setAtualizadoEm(category.getAtualizadoEm());
        return dto;
    }
}