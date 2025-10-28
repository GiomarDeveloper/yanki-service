package com.bank.yanki.domain.repository;

import com.bank.yanki.domain.model.YankiWallet;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface YankiWalletRepository extends ReactiveMongoRepository<YankiWallet, String> {

    Mono<YankiWallet> findByPhoneNumber(String phoneNumber);
    Mono<YankiWallet> findByDocumentNumber(String documentNumber);
    Mono<Boolean> existsByPhoneNumber(String phoneNumber);
    Mono<Boolean> existsByDocumentNumber(String documentNumber);
}