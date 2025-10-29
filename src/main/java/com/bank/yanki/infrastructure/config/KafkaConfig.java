package com.bank.yanki.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.SenderOptions;

/**
 * Configuración de Apache Kafka para el servicio Yanki.
 *
 * <p>Esta clase configura los topics de Kafka y el productor reactivo
 * utilizado para publicar eventos en el sistema.</p>
 *
 */
@Configuration
public class KafkaConfig {

  @Value("${app.kafka.topics.transaction-created:yanki.transactions.created}")
  private String transactionTopic;

  @Value("${app.kafka.topics.wallet-updated:yanki.wallet.updated}")
  private String walletTopic;

  @Value("${app.kafka.topics.card-associated:yanki.card.associated}")
  private String cardTopic;

  @Value("${app.kafka.topics.notification:yanki.notifications}")
  private String notificationTopic;

  @Value("${spring.kafka.bootstrap-servers:localhost:29093}")
  private String bootstrapServers;

  /**
   * Configura el topic para transacciones creadas.
   *
   * @return Nueva configuración de topic
   */
  // Tus topics existentes
  @Bean
  public NewTopic transactionTopic() {
    return TopicBuilder.name(transactionTopic)
      .partitions(3)
      .replicas(1)
      .build();
  }

  /**
   * Configura el topic para actualizaciones de billeteras.
   *
   * @return Nueva configuración de topic
   */
  @Bean
  public NewTopic walletTopic() {
    return TopicBuilder.name(walletTopic)
      .partitions(2)
      .replicas(1)
      .build();
  }

  /**
   * Configura el topic para asociaciones de tarjetas.
   *
   * @return Nueva configuración de topic
   */
  @Bean
  public NewTopic cardTopic() {
    return TopicBuilder.name(cardTopic)
      .partitions(2)
      .replicas(1)
      .build();
  }

  /**
   * Configura el topic para notificaciones.
   *
   * @return Nueva configuración de topic
   */
  @Bean
  public NewTopic notificationTopic() {
    return TopicBuilder.name(notificationTopic)
      .partitions(1)
      .replicas(1)
      .build();
  }

  /**
   * Configura el topic para configs en kafka.
   *
   * @return Nueva configuración de topic
   */
  @Bean
  public ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaProducerTemplate() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

    // Configuraciones opcionales para mejor rendimiento
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.RETRIES_CONFIG, 3);

    return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(props));
  }
}