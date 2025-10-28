package com.bank.yanki.domain.exception;

public class DuplicateWalletException extends RuntimeException {
    public DuplicateWalletException(String field, String value) {
        super(String.format("Wallet already exists with %s: %s", field, value));
    }
}