package com.villaggiogirotto.split.villagiosplit.dto;

import lombok.Data;

@Data
public class PixDTO {
    private Integer expiresIn; // Tempo de expiração em segundos (default: 86400 = 24h)
}