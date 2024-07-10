package com.server.exchangerates.exchanges.Configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ExchangeConfig {
    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder){
        return webClientBuilder.baseUrl("https://v6.exchangerate-api.com/v6/").build();
    }
}
