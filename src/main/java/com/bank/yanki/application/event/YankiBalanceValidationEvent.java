package com.bank.yanki.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YankiBalanceValidationEvent {
    private String validationId;
    private String requestService;
    private String phoneNumber;
    private Double requiredAmount;
    private String currency;
    private Long timestamp;
}