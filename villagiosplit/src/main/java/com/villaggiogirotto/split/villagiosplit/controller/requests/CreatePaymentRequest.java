package com.villaggiogirotto.split.villagiosplit.controller.requests;

import com.villaggiogirotto.split.villagiosplit.dto.CartItemDTO;
import com.villaggiogirotto.split.villagiosplit.dto.SplitInputDTO;
import lombok.Data;

import java.util.List;

@Data
public class CreatePaymentRequest {

    private Integer amount;
    private Integer installments;
    private List<CartItemDTO> items;
    private List<SplitInputDTO> split;
}
