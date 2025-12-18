package com.villaggiogirotto.split.villagiosplit.dto;

import lombok.Data;

@Data
public class DebitCardDTO {
    private String number;
    private String holderName;
    private Integer expMonth;
    private Integer expYear;
    private String cvv;
    private AddressDTO billingAddress;
    private String statementDescriptor;
}
