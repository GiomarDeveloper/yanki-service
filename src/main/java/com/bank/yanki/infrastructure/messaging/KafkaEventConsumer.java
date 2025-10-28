package com.bank.yanki.infrastructure.messaging;

import com.bank.yanki.application.event.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {

    @KafkaListener(
            topics = "${app.kafka.topics.transaction-created:yanki.transactions.created}",
            groupId = "${spring.kafka.consumer.group-id:yanki-service}"
    )
    public void consumeTransactionCreated(TransactionCreatedEvent event) {
        log.info("Received transaction created event: {}", event.getTransactionId());
        // Aquí podrías procesar notificaciones, actualizar dashboards, etc.
        // Pero NO llamar a otros microservicios via REST
    }

    @KafkaListener(
            topics = "${app.kafka.topics.wallet-updated:yanki.wallet.updated}",
            groupId = "${spring.kafka.consumer.group-id:yanki-service}"
    )
    public void consumeWalletUpdated(String walletId) {
        log.info("Received wallet updated event: {}", walletId);
        // Invalidar cache si es necesario
    }
}