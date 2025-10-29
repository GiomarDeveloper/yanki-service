package com.bank.yanki.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YankiPaymentCompletedEvent {
    private String paymentId;
    private String requestId;
    private boolean success;
    private String message;
    private Long timestamp;
}