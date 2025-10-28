package com.bank.yanki.infrastructure.config;

import com.bank.yanki.domain.model.YankiWallet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    @Bean
    public ReactiveRedisTemplate<String, YankiWallet> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        // Configurar ObjectMapper con soporte para Java Time
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Especificar el tipo YankiWallet en el serializer
        Jackson2JsonRedisSerializer<YankiWallet> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, YankiWallet.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, YankiWallet> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, YankiWallet> context =
                builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}