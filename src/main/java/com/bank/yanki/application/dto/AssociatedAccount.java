package com.bank.yanki.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssociatedAccount {
    private String accountId;
    private Integer sequenceOrder;
    private LocalDateTime associatedAt;
    private String status;
}