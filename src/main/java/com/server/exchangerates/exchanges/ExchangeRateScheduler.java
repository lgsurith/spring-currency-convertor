package com.server.exchangerates.exchanges;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.server.exchangerates.exchanges.Service.ExchangeService;

@Component
public class ExchangeRateScheduler {
    private final ExchangeService exchangeService;

    public ExchangeRateScheduler(ExchangeService exchangeService){
        this.exchangeService = exchangeService;
    }

    //setting up the cron operation for everyday at 12:00 AM
    @Scheduled(cron = "0 0 0 * * *")
    public void updateExchangeRates(){
        exchangeService.fetchAndCacheLatestRates();
    }
}
