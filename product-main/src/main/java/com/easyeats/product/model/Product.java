package com.easyeats.product.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.easyeats.product.dto.ProductDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_produto", schema = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nome;
    private Float preco;
    private String descricao;
    private String productIdentifier;

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