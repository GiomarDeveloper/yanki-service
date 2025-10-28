package com.bank.yanki.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditPaymentResponseEvent {
    private String paymentId;
    private String creditId;
    private Boolean success;
    private String transactionId;
    private BigDecimal newBalance;
    private String message;
    private LocalDateTime timestamp;
}