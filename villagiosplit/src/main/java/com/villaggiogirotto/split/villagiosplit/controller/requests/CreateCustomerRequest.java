package com.villaggiogirotto.split.villagiosplit.controller.requests;

import com.villaggiogirotto.split.villagiosplit.dto.AddressDTO;
import lombok.Data;

import java.util.Map;

@Data
public class CreateCustomerRequest {
    private String filialId; // ID da filial (obrigatório)
    
    // Dados do cliente
    private String name; // Obrigatório - Max: 64 caracteres
    private String email; // Max: 64 caracteres
    private String document; // CPF, CNPJ ou PASSPORT
    private String documentType; // "CPF", "CNPJ" ou "PASSPORT"
    private String type; // "individual" ou "company"
    private String code; // Código no sistema da loja - Max: 52 caracteres
    private String gender; // "male" ou "female"
    private String birthdate; // Formato: MM/DD/YYYY
    
    // Endereço (opcional)
    private AddressDTO address;
    
    // Telefones (opcional)
    private Map<String, Object> phones;
    
    // Metadata (opcional)
    private Map<String, String> metadata;
}
