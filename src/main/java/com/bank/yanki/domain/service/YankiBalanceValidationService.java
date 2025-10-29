package com.bank.yanki.domain.service;

import com.bank.yanki.application.event.YankiBalanceValidationEvent;
import com.bank.yanki.application.event.YankiBalanceValidationResponse;
import reactor.core.publisher.Mono;

public interface YankiBalanceValidationService {
    Mono<Void> processBalanceValidation(YankiBalanceValidationEvent event);
    void processBalanceValidationResponse(YankiBalanceValidationResponse response);
}