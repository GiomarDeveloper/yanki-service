package com.bank.yanki.domain.service.impl;

import com.bank.yanki.application.event.YankiPaymentCompletedEvent;
import com.bank.yanki.application.event.YankiPaymentRequestEvent;
import com.bank.yanki.domain.model.YankiTransaction;
import com.bank.yanki.domain.model.YankiWallet;
import com.bank.yanki.domain.service.YankiPaymentService;
import com.bank.yanki.infrastructure.messaging.KafkaEventProducer;
import com.bank.yanki.domain.repository.YankiWalletRepository;
import com.bank.yanki.domain.repository.YankiTransactionRepository;
import com.bank.yanki.model.TransactionStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class YankiPaymentServiceImpl implements YankiPaymentService {

    private final YankiWalletRepository walletRepository;
    private final YankiTransactionRepository transactionRepository;
    private final KafkaEventProducer kafkaProducer;

    @Override
    public Mono<Void> processYankiPayment(YankiPaymentRequestEvent event) {
        log.info("💰 Processing Yanki payment - PaymentId: {}, From: {}, To: {}, Amount: {}",
                event.getPaymentId(), event.getFromPhoneNumber(), event.getToPhoneNumber(), event.getAmount());

        return Mono.zip(
                walletRepository.findByPhoneNumber(event.getFromPhoneNumber()),
                walletRepository.findByPhoneNumber(event.getToPhoneNumber())
        ).flatMap(tuple -> {
            YankiWallet fromWallet = tuple.getT1();
            YankiWallet toWallet = tuple.getT2();

            // Validar que ambos wallets existen
            if (fromWallet == null || toWallet == null) {
                String errorMsg = "Wallet no encontrado: " +
                        (fromWallet == null ? event.getFromPhoneNumber() : event.getToPhoneNumber());
                log.error("❌ {}", errorMsg);
                return sendPaymentResponse(event, false, errorMsg);
            }

            // Validar saldo del remitente
            BigDecimal amount = BigDecimal.valueOf(event.getAmount());
            if (!fromWallet.hasSufficientBalance(amount)) {
                String errorMsg = String.format("Saldo insuficiente. Disponible: %.2f, Requerido: %.2f",
                        fromWallet.getBalance(), amount);
                log.error("❌ {}", errorMsg);
                return sendPaymentResponse(event, false, errorMsg);
            }

            // Procesar la transferencia
            return processTransfer(fromWallet, toWallet, amount, event);
        }).onErrorResume(ex -> {
            log.error("❌ Error processing Yanki payment: {}", ex.getMessage());
            return sendPaymentResponse(event, false, "Error procesando pago: " + ex.getMessage());
        });
    }

    private Mono<Void> processTransfer(YankiWallet fromWallet, YankiWallet toWallet,
                                       BigDecimal amount, YankiPaymentRequestEvent event) {
        // Realizar la transferencia
        fromWallet.updateBalance(amount, false); // Restar del remitente
        toWallet.updateBalance(amount, true);    // Sumar al destinatario

        return walletRepository.save(fromWallet)
                .then(walletRepository.save(toWallet))
                .then(createTransactionRecord(fromWallet, toWallet, amount, event))
                .then(sendPaymentResponse(event, true, "Pago Yanki procesado exitosamente"))
                .doOnSuccess(v -> log.info("✅ Yanki payment completed - PaymentId: {}", event.getPaymentId()));
    }

    private Mono<YankiTransaction> createTransactionRecord(YankiWallet fromWallet, YankiWallet toWallet,
                                                           BigDecimal amount, YankiPaymentRequestEvent event) {
        YankiTransaction transaction = YankiTransaction.builder()
                .id(UUID.randomUUID().toString())
                .transactionId(event.getPaymentId())
                .amount(amount)
                .fromWalletId(fromWallet.getId())
                .toWalletId(toWallet.getId())
                .fromPhoneNumber(fromWallet.getPhoneNumber())
                .toPhoneNumber(toWallet.getPhoneNumber())
                .description(event.getDescription())
                .type(YankiTransaction.TransactionType.TRANSFER)
                .status(TransactionStatusEnum.COMPLETED)
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction)
                .doOnSuccess(t -> log.info("📝 Transaction record created - TransactionId: {}", t.getTransactionId()));
    }

    private Mono<Void> sendPaymentResponse(YankiPaymentRequestEvent event, boolean success, String message) {
        YankiPaymentCompletedEvent response = YankiPaymentCompletedEvent.builder()
                .paymentId(event.getPaymentId())
                .requestId(event.getRequestId())
                .success(success)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();

        return kafkaProducer.sendYankiPaymentResponse(response)
                .doOnSuccess(v -> {
                    if (success) {
                        log.info("✅ Yanki payment response sent - PaymentId: {}", event.getPaymentId());
                    } else {
                        log.error("❌ Yanki payment failed response sent - PaymentId: {}", event.getPaymentId());
                    }
                });
    }
}