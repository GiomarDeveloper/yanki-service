package com.bank.yanki.application.dto;

import com.bank.yanki.domain.model.YankiWallet;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public  class PendingTransaction {
    private YankiWallet fromWallet;
    private String toPhoneNumber;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
    private String transactionId;
}