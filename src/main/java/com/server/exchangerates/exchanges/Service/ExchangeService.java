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
        ExchangeResponse rates = webClient.get()
                .uri("/{apikey}/latest/{defaultCurrency}", apikey, defaultCurrency)
                .retrieve()
                .bodyToMono(ExchangeResponse.class)
                .block();
        
        String cacheKey = "exchange_rates_" + LocalDate.now();
        redisTemplate.opsForValue().set(cacheKey , rates , Duration.ofDays(2));
    }

    public Mono<ExchangeResponse> getRates(){
        //adding into log files.
        logger.debug("Fetching Exchange Rates");
        String cacheKey = "exchange_rates_" + LocalDate.now();
        ExchangeResponse cachedRates = redisTemplate.opsForValue().get(cacheKey);
        
        //checks for the rates in the cache.
        if(cachedRates != null){
            return Mono.just(cachedRates);
        }else{
            return fetchAndCacheLatestRates2();
        }
    }

    //if not in cache then fetch from the api and cache it accordingly.
    public Mono<ExchangeResponse> fetchAndCacheLatestRates2(){
        return webClient.get()
                .uri("/{apikey}/latest/{defaultCurrency}", apikey, defaultCurrency)
                .retrieve()
                .bodyToMono(ExchangeResponse.class)
                .doOnNext(rates -> {
                    String cacheKey = "exchange_rates_" + LocalDate.now();
                    redisTemplate.opsForValue().set(cacheKey, rates, Duration.ofDays(2));
                });
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
}