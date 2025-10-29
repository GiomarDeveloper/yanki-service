package com.bank.yanki.infrastructure.messaging;

import com.bank.yanki.application.event.CreditBalanceResponseEvent;
import com.bank.yanki.domain.service.YankiWalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de Kafka para procesar respuestas de validación de saldo de créditos.
 * Este componente escucha mensajes del tópico "yanki.credit.balance.response"
 * y procesa las respuestas de validación de saldo para operaciones Yanki.
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditBalanceResponseConsumer {

  private final YankiWalletService yankiWalletService;
  private final ObjectMapper objectMapper;

  /**
   * Consume y procesa mensajes de respuesta de validación de saldo de crédito.
   * Este método se ejecuta automáticamente cuando llega un mensaje al tópico
   * "yanki.credit.balance.response". Deserializa el mensaje JSON y delega el
   * procesamiento al servicio correspondiente.
   *
   * @param message El mensaje JSON recibido de Kafka como String
   * @throws Exception Si ocurre un error durante la deserialización o procesamiento
   */
  @KafkaListener(topics = "yanki.credit.balance.response", groupId = "yanki-service")
  public void consumeCreditBalanceResponse(String message) {
    try {
      log.info("📨 Received credit balance response raw message: {}", message);

      // Deserializar manualmente desde String
      CreditBalanceResponseEvent event =
        objectMapper.readValue(message, CreditBalanceResponseEvent.class);

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