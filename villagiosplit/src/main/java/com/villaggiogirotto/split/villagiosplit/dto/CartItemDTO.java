package com.villaggiogirotto.split.villagiosplit.dto;

import lombok.Data;

@Data
public class CartItemDTO {

    private String name;
    private String description;
    private Integer amount;
    private Integer defaultQuantity;
}
