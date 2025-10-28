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
public class CreditBalanceResponseEvent {
    private String inquiryId;
    private String creditId;
    private Boolean isValid;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;
    private String currency;
    private LocalDateTime timestamp;
    private String reason;
    private String transactionId;
}