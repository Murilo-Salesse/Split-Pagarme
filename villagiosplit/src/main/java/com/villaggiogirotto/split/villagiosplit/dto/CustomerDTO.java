package com.villaggiogirotto.split.villagiosplit.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CustomerDTO {
    private String name; // required
    private String email;
    private String type; // "individual" ou "company"
    private String document; // CPF/CNPJ
    private String documentType; // "CPF", "CNPJ", "PASSPORT"
    private String code; // Código no seu sistema
    private String gender; // "male" ou "female"
    private String birthdate; // formato: YYYY-MM-DD
    private AddressDTO address;
    private Map<String, Object> phones;
    private Map<String, String> metadata;
}