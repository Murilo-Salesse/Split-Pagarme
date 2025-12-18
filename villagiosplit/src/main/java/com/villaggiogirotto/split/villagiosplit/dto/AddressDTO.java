package com.villaggiogirotto.split.villagiosplit.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private String country; // Ex: "BR"
    private String state; // Ex: "SP"
    private String city;
    private String zipCode; // Apenas números
    private String line1; // Número, Rua, Bairro (separados por vírgula)
    private String line2; // Complemento
}