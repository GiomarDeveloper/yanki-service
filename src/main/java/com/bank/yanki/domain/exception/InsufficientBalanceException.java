package com.bank.yanki.domain.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(BigDecimal currentBalance, BigDecimal requiredAmount) {
        super(String.format("Insufficient balance. Current: %s, Required: %s", currentBalance, requiredAmount));
    }
}