package com.bank.yanki.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YankiBalanceValidationResponse {
    private String validationId;
    private String requestService;
    private String phoneNumber;
    private Double currentBalance;
    private Double requiredAmount;
    private Boolean sufficientBalance;
    private String status;
    private String message;
    private Long timestamp;
}