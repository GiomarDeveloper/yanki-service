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
public class CreditPaymentRequestEvent {
    private String paymentId;
    private String creditId;
    private String walletId;
    private String phoneNumber;
    private BigDecimal amount;
    private String description;
    private LocalDateTime timestamp;
}