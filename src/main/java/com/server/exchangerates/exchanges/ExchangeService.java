package com.server.exchangerates.exchanges;

import java.time.Duration;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class ExchangeService {
    private final WebClient webClient;
    private final RedisTemplate<String , Object> redisTemplate;
    private final String apikey;
    private final String defaultCurrency;

    public ExchangeService(WebClient webClient,
                        RedisTemplate<String, Object> redisTemplate,
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
        String cacheKey = "exchange_rates_" + LocalDate.now();
        ExchangeResponse cachedRates = (ExchangeResponse) redisTemplate.opsForValue().get(cacheKey);

        if(cachedRates != null){
            return Mono.just(cachedRates);
        }else{
            return fetchAndCacheLatestRates2();
        }
    }

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
                .map(response -> {
                    Double fromRate = response.getRates().get(from);
                    Double toRate = response.getRates().get(to);
                    Double convertedAmount = (amount / fromRate) * toRate;
                    return new ConversionResponse(from, to, amount, convertedAmount);
                });
    }                                                      
}
