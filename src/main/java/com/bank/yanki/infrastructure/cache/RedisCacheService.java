package com.bank.yanki.infrastructure.cache;

import com.bank.yanki.domain.model.YankiWallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final ReactiveRedisTemplate<String, YankiWallet> redisTemplate;

    private static final Duration WALLET_TTL = Duration.ofHours(1);

    public Mono<Boolean> cacheWallet(String phoneNumber, YankiWallet wallet) {
        String key = "wallet:" + phoneNumber;
        return redisTemplate.opsForValue()
                .set(key, wallet, WALLET_TTL)
                .doOnSuccess(result ->
                        log.debug("Wallet cached for phone: {}", phoneNumber)
                )
                .doOnError(error ->
                        log.error("Error caching wallet: {}", error.getMessage())
                );
    }

    public Mono<YankiWallet> getCachedWallet(String phoneNumber) {
        String key = "wallet:" + phoneNumber;
        return redisTemplate.opsForValue()
                .get(key)
                .doOnSuccess(wallet -> {
                    if (wallet != null) {
                        log.debug("Wallet cache hit for phone: {}", phoneNumber);
                    }
                });
    }

    public Mono<Boolean> evictWalletCache(String phoneNumber) {
        String key = "wallet:" + phoneNumber;
        return redisTemplate.delete(key)
                .map(count -> count > 0)
                .doOnSuccess(result ->
                        log.debug("Wallet cache evicted for phone: {}", phoneNumber)
                );
    }
}