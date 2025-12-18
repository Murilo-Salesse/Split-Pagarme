package com.villaggiogirotto.split.villagiosplit.dto;

import lombok.Data;

@Data
public class BoletoDTO {
    private String instructions; // Instruções no boleto
    private String dueAt; // Data de vencimento (formato: YYYY-MM-DD)
}