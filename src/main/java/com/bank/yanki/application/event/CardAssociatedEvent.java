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
public class CardAssociatedEvent {
    private String walletId;
    private String phoneNumber;
    private String creditId;
    private LocalDateTime associatedAt;
}