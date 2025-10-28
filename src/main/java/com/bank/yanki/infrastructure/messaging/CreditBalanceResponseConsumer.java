package com.bank.yanki.infrastructure.messaging;

import com.bank.yanki.application.event.CreditBalanceResponseEvent;
import com.bank.yanki.domain.service.YankiWalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditBalanceResponseConsumer {

    private final YankiWalletService yankiWalletService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "yanki.credit.balance.response", groupId = "yanki-service")
    public void consumeCreditBalanceResponse(String message) {
        try {
            log.info("📨 Received credit balance response raw message: {}", message);

            // Deserializar manualmente desde String
            CreditBalanceResponseEvent event = objectMapper.readValue(message, CreditBalanceResponseEvent.class);

            log.info("🎯 Parsed credit balance response - InquiryId: {}, IsValid: {}, Reason: {}",
                    event.getInquiryId(), event.getIsValid(), event.getReason());

            // Procesar la respuesta de validación de saldo
            yankiWalletService.processCreditBalanceResponse(event)
                    .subscribe(result ->
                            log.info("✅ Credit balance response processed - InquiryId: {}", event.getInquiryId())
                    );

        } catch (Exception e) {
            log.error("❌ Error processing credit balance response: {}", e.getMessage(), e);
        }
    }
}