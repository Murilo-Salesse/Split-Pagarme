package com.villaggiogirotto.split.villagiosplit.controller.requests;

import com.villaggiogirotto.split.villagiosplit.dto.*;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CreateOrderRequest {
    private String filialId;
    private String code; // Identificador no seu sistema
    private Integer amount; // Fallback se não enviar items

    // Customer
    private String customerId; // ID de cliente já cadastrado
    private CustomerDTO customer; // Ou dados do novo cliente

    // Items
    private List<CartItemDTO> items;

    // Payment
    private String paymentMethod; // "credit_card", "pix", "boleto", "debit_card"
    private CreditCardDTO creditCard;
    private PixDTO pix;
    private BoletoDTO boleto;
    private DebitCardDTO debitCard;

    // Split
    private List<SplitInputDTO> split;

    // Optional
    private Boolean closed; // default: true
    private ShippingDTO shipping;
    private Map<String, String> metadata;
}