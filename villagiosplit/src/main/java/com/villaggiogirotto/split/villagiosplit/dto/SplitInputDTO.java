package com.villaggiogirotto.split.villagiosplit.dto;

import lombok.Data;

@Data
public class SplitInputDTO {

    private String recipientId;
    private Integer amount;
    private Boolean liable;


}
