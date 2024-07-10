package com.server.exchangerates.exchanges.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/exchange")
public class Exchangecontroller {
    private final ExchangeService exchangeService;

    public Exchangecontroller(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping("/test")
    public String test(){
        return "This Port is functional";
    }

    //to obtain the exchange rates for the given day.
    @GetMapping("/rates")
    public Mono<ExchangeResponse> getRates(){
        return exchangeService.getRates();
    }

    //convert any type of currencies from the given rates.
    @GetMapping("/convert")
    public Mono<ConversionResponse> convert(
        @RequestParam String from,
        @RequestParam String to,
        @RequestParam Double amount){
            return exchangeService.convert(from, to, amount);
        }
    
}
