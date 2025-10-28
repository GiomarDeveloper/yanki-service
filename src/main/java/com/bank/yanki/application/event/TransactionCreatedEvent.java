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
public class TransactionCreatedEvent {
    private String transactionId;
    private String fromWalletId;
    private String toWalletId;
    private String fromPhoneNumber;
    private String toPhoneNumber;
    private BigDecimal amount;
    private String type;
    private String description;
    private LocalDateTime transactionDate;

    private String creditId;
    private String customerId;
    private String source;
}