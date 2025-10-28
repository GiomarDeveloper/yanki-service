package com.bank.yanki.infrastructure.web;

import com.bank.yanki.api.WalletsApi;
import com.bank.yanki.application.mapper.YankiWalletMapper;
import com.bank.yanki.domain.model.Transaction;
import com.bank.yanki.domain.model.YankiWallet;
import com.bank.yanki.domain.service.YankiWalletService;
import com.bank.yanki.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class YankiController implements WalletsApi {

    private final YankiWalletService walletService;
    private final YankiWalletMapper walletMapper;

    @Override
    public Mono<ResponseEntity<CardAssociationResponse>> associateCard(Mono<CardAssociationRequest> cardAssociationRequest, ServerWebExchange exchange) {
        return cardAssociationRequest
                .flatMap(request ->
                        walletService.associateDebitCard(request.getPhoneNumber(), request.getCreditId())
                )
                .map(walletMapper::toCardAssociationResponse)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Card associated successfully"))
                .doOnError(error -> log.error("Error associating card: {}", error.getMessage()))
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @Override
    public Mono<ResponseEntity<YankiWalletResponse>> createWallet(Mono<YankiWalletRequest> yankiWalletRequest, ServerWebExchange exchange) {
        return yankiWalletRequest
                .map(walletMapper::toDomain)
                .flatMap(walletService::createWallet)
                .map(walletMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .doOnSuccess(response -> log.info("Wallet created successfully"))
                .doOnError(error -> log.error("Error creating wallet: {}", error.getMessage()))
                .onErrorResume(error -> {
                    if (error.getMessage().contains("already exists")) {
                        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                });
    }

    @Override
    public Mono<ResponseEntity<YankiWalletResponse>> getWalletByPhone(String phoneNumber, ServerWebExchange exchange) {
        return walletService.findByPhoneNumber(phoneNumber)
                .map(walletMapper::toResponse)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Wallet retrieved for phone: {}", phoneNumber))
                .doOnError(error -> log.error("Error retrieving wallet: {}", error.getMessage()))
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> sendPayment(
            Mono<SendPaymentRequest> sendPaymentRequest,
            ServerWebExchange exchange) {

        return sendPaymentRequest
                .flatMap(request -> walletService.processPayment(
                        request.getFromPhoneNumber(),
                        request.getToPhoneNumber(),
                        request.getAmount(),
                        request.getDescription()
                ))
                .flatMap(transaction -> {
                    TransactionResponse response = walletMapper.toTransactionResponse(transaction);

                    if (transaction.getStatus() == TransactionStatusEnum.PENDING) {
                        // Caso asíncrono - 202 Accepted
                        return Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED).body(response));
                    } else {
                        // Caso inmediato - 200 OK
                        return Mono.just(ResponseEntity.ok(response));
                    }
                })
                .doOnSuccess(response -> {
                    if (response.getStatusCode() == HttpStatus.ACCEPTED) {
                        log.info("Payment validation in progress - TransactionId: {}", response.getBody().getId());
                    } else {
                        log.info("Payment processed successfully - TransactionId: {}", response.getBody().getId());
                    }
                })
                .doOnError(error -> log.error("Error processing payment: {}", error.getMessage()))
                .onErrorResume(error -> {
                    if (error.getMessage().contains("not found")) {
                        return Mono.just(ResponseEntity.notFound().build());
                    } else if (error.getMessage().contains("insufficient balance") ||
                            error.getMessage().contains("Insufficient")) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build());
                    }
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }
}
