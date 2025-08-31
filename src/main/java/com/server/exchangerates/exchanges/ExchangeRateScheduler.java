package com.server.exchangerates.exchanges;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.server.exchangerates.exchanges.Service.ExchangeService;

@Component
public class ExchangeRateScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateScheduler.class);
    private final ExchangeService exchangeService;

    public ExchangeRateScheduler(ExchangeService exchangeService){
        this.exchangeService = exchangeService;
    }

    //setting up the cron operation for every minute to get fresh exchange rates
    @Scheduled(cron = "0 * * * * *")
    public void updateExchangeRates(){
        logger.debug("Scheduled task: Updating exchange rates from API");
        exchangeService.fetchAndCacheLatestRates();
        logger.debug("Scheduled task: Exchange rates updated and cached");
    }

    //cleanup old keys daily at 12:05 AM to prevent memory accumulation
    @Scheduled(cron = "0 5 0 * * *")
    public void cleanupOldKeys(){
        logger.info("Scheduled cleanup: Starting removal of old exchange rate keys");
        exchangeService.cleanupOldExchangeRates();
        logger.info("Scheduled cleanup: Completed");
    }
}
