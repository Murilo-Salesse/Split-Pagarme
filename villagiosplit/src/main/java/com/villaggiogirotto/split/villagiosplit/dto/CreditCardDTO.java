package com.villaggiogirotto.split.villagiosplit.dto;

import lombok.Data;

@Data
public class CreditCardDTO {
    // Opção 1: Usar cartão já cadastrado
    private String cardId;

    // Opção 2: Usar token de cartão
    private String cardToken;

    // Opção 3: Dados completos do cartão (use apenas para testes!)
    private String number;
    private String holderName;
    private Integer expMonth;
    private Integer expYear;
    private String cvv;
    private AddressDTO billingAddress; // Obrigatório para split com antifraude

    // Configurações
    private Integer installments; // Número de parcelas
    private String operationType; // "auth_only" ou "auth_and_capture" (default)
    private String statementDescriptor; // Aparece na fatura
}
