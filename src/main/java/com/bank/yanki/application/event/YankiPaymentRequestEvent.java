package com.bank.yanki.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YankiPaymentRequestEvent {
    private String paymentId;
    private String requestId;
    private String fromPhoneNumber;
    private String toPhoneNumber;
    private Double amount;
    private String description;
    private String currency;
    private Long timestamp;
}