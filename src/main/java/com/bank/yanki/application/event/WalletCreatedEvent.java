package com.bank.yanki.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletCreatedEvent {
    private String walletId;
    private String phoneNumber;
    private String documentNumber;
    private String documentType;
    private String email;
    private LocalDateTime createdAt;
}