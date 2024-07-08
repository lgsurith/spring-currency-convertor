package com.server.exchangerates.exchanges;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ExchangeConfig {
    @Bean
    public  WebClient.Builder webClientBuilder(){
        return WebClient.builder();
    }
}
