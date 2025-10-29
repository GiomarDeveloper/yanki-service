package com.bank.yanki.domain.service;

import com.bank.yanki.application.event.YankiPaymentRequestEvent;
import reactor.core.publisher.Mono;

public interface YankiPaymentService {
    Mono<Void> processYankiPayment(YankiPaymentRequestEvent event);
}