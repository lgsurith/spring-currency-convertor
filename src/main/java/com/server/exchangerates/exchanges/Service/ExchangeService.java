package com.server.exchangerates.exchanges.Service;

import java.time.Duration;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;

import com.server.exchangerates.exchanges.Response.ExchangeResponse;
import com.server.exchangerates.exchanges.Response.ConversionResponse;

@Service
public class ExchangeService {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeService.class);
    private final WebClient webClient;
    private final RedisTemplate<String , ExchangeResponse> redisTemplate;
    private final String apikey;
    private final String defaultCurrency;

    public ExchangeService(WebClient webClient,
                        RedisTemplate<String, ExchangeResponse> redisTemplate,
                        @Value("${exchange.api.key}") String apikey,
                        @Value("${exchange.api.defaultcurrency}") String defaultCurrency){
                            this.webClient = webClient;
                            this.redisTemplate = redisTemplate;
                            this.apikey = apikey;
                            this.defaultCurrency = defaultCurrency;
    }

    public void fetchAndCacheLatestRates(){
        String cacheKey = "exchange_rates_" + LocalDate.now();
        logger.debug("Scheduler: Fetching fresh exchange rates for key: {}", cacheKey);
        
        ExchangeResponse rates = webClient.get()
                .uri("/{apikey}/latest/{defaultCurrency}", apikey, defaultCurrency)
                .retrieve()
                .bodyToMono(ExchangeResponse.class)
                .block();
        
        redisTemplate.opsForValue().set(cacheKey , rates , Duration.ofMinutes(1));
        logger.info("Scheduler: Cached fresh exchange rates with 1-minute TTL for key: {}", cacheKey);
    }

    public Mono<ExchangeResponse> getRates(){
        //adding into log files.
        logger.debug("Fetching Exchange Rates");
        String cacheKey = "exchange_rates_" + LocalDate.now();
        ExchangeResponse cachedRates = redisTemplate.opsForValue().get(cacheKey);
        
        //checks for the rates in the cache.
        if(cachedRates != null){
            logger.debug("Exchange rates found in Redis cache for key: {}", cacheKey);
            return Mono.just(cachedRates);
        }else{
            logger.debug("Exchange rates not found in cache, fetching from API for key: {}", cacheKey);
            return fetchAndCacheLatestRates2();
        }
    }

    //if not in cache then fetch from the api and cache it accordingly.
    public Mono<ExchangeResponse> fetchAndCacheLatestRates2(){
        String cacheKey = "exchange_rates_" + LocalDate.now();
        logger.debug("Fetching fresh exchange rates from API for key: {}", cacheKey);
        
        return webClient.get()
                .uri("/{apikey}/latest/{defaultCurrency}", apikey, defaultCurrency)
                .retrieve()
                .bodyToMono(ExchangeResponse.class)
                .doOnNext(rates -> {
                    redisTemplate.opsForValue().set(cacheKey, rates, Duration.ofMinutes(1));
                    logger.info("Fresh exchange rates cached in Redis with 1-minute TTL for key: {}", cacheKey);
                })
                .doOnError(error -> logger.error("Failed to fetch exchange rates from API", error));
    }
    
    public Mono<ConversionResponse> convert(String from , String to , Double amount){
        return getRates()
                .flatMap(response -> {
                    Double fromRate = response.getRates().get(from);
                    Double toRate = response.getRates().get(to);
                    if(fromRate == null || toRate == null){
                        return Mono.error(new IllegalArgumentException("Invalid :( , make sure to enter the correct currency code."));
                    }
                    Double convertedAmount = (amount / fromRate) * toRate;
                    return Mono.just(new ConversionResponse(from, to, amount, convertedAmount));
                });
    }

    // Cleanup method to prevent memory accumulation
    public void cleanupOldExchangeRates() {
        logger.info("Starting cleanup of old exchange rate keys...");
        try {
            int cleanedCount = 0;
            // Delete keys older than 7 days
            for (int i = 7; i <= 30; i++) { // Clean up to 30 days back to be safe
                String oldKey = "exchange_rates_" + LocalDate.now().minusDays(i);
                Boolean deleted = redisTemplate.delete(oldKey);
                if (Boolean.TRUE.equals(deleted)) {
                    cleanedCount++;
                    logger.debug("Cleaned up old exchange rate key: {}", oldKey);
                }
            }
            logger.info("Cleanup completed: {} old exchange rate keys removed", cleanedCount);
        } catch (Exception e) {
            logger.error("Error during cleanup of old exchange rate keys", e);
        }
    }                                                      
}