package com.villaggiogirotto.split.villagiosplit.dto;

import lombok.Data;

@Data
public class CartItemDTO {
    private String name;
    private String description;
    private Integer amount; // Valor unitário em centavos
    private Integer defaultQuantity;
    private String code; // Código do item no seu sistema (gerado automaticamente se não enviado)
}