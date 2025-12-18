package com.villaggiogirotto.split.villagiosplit.dto;

import lombok.Data;

@Data
public class ShippingDTO {
    private Integer amount; // Valor da entrega em centavos
    private String description;
    private String recipientName;
    private String recipientPhone;
    private AddressDTO address;
}