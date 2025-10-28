package com.bank.yanki.domain.exception;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String message) {
        super(message);
    }

    public WalletNotFoundException(String phoneNumber, String documentNumber) {
        super(String.format("Wallet not found with phone: %s or document: %s", phoneNumber, documentNumber));
    }
}