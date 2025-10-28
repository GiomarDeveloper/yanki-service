package com.bank.yanki.domain.model;

import com.bank.yanki.model.TransactionStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private String id;
    private BigDecimal amount;
    private String fromWalletId;
    private String toWalletId;
    private String fromPhoneNumber;
    private String toPhoneNumber;
    private String description;
    private TransactionStatusEnum status;
    private LocalDateTime transactionDate;
}