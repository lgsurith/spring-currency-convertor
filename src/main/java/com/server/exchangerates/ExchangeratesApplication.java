package com.server.exchangerates;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExchangeratesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExchangeratesApplication.class, args);
	}
	
}
