package com.bank.yanki.domain.repository;

import com.bank.yanki.domain.model.YankiTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface YankiTransactionRepository extends ReactiveMongoRepository<YankiTransaction, String> {
    Flux<YankiTransaction> findByFromPhoneNumber(String fromPhoneNumber);
    Flux<YankiTransaction> findByToPhoneNumber(String toPhoneNumber);
    Flux<YankiTransaction> findByFromWalletId(String fromWalletId);
    Flux<YankiTransaction> findByToWalletId(String toWalletId);
    Mono<YankiTransaction> findByTransactionId(String transactionId);
}