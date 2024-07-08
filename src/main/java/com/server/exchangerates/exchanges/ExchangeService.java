package com.server.exchangerates.exchanges;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;


@Service
public class ExchangeService {
    private final WebClient webClient;
    private final String apikey;
    private final String defaultCurrency;

    public ExchangeService(WebClient.Builder webClientBuilder,
                        @Value("${exchange.api.url}") String baseUrl,
                        @Value("${exchange.api.key}") String apikey,
                        @Value("${exchange.api.defaultcurrency}") String defaultCurrency){
                            this.webClient = webClientBuilder.baseUrl(baseUrl).build();
                            this.apikey = apikey;
                            this.defaultCurrency = defaultCurrency;
                        }

    public Mono<ExchangeResponse> getRates(){
        return webClient.get()
                .uri("/{apikey}/latest/{defaultCurrency}", apikey, defaultCurrency)
                .retrieve()
                .bodyToMono(ExchangeResponse.class);
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
