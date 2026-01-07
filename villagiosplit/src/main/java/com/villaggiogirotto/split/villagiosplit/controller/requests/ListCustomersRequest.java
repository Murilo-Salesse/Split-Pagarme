package com.villaggiogirotto.split.villagiosplit.controller.requests;

import lombok.Data;

@Data
public class ListCustomersRequest {
    private String filialId; // ID da filial (obrigatório)
    
    // Filtros opcionais
    private String name;
    private String document;
    private String email;
    private String gender;
    private String code;
    
    // Paginação
    private Integer page = 1;
    private Integer size = 10;
}
