package com.easyeats.product.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.easyeats.product.dto.ProductDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "tb_produto", schema = "products",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_produto_identifier_categoria",
                columnNames = { "product_identifier", "categoria_id" }))
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nome;

    @Column(precision = 12, scale = 2)
    private BigDecimal preco;

    private String descricao;

    @Column(name = "product_identifier")
    private String productIdentifier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public static Product convert(ProductDto productDto) {
        Product product = new Product();
        product.setNome(productDto.getNome());
        product.setPreco(productDto.getPreco());
        product.setDescricao(productDto.getDescricao());
        product.setProductIdentifier(productDto.getProductIdentifier());
        product.setStatus(productDto.getStatus() != null
                ? productDto.getStatus()
                : ProductStatus.ATIVO);
        return product;
    }
}