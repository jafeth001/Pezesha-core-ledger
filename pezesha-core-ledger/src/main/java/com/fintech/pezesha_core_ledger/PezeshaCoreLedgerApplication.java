package com.fintech.pezesha_core_ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PezeshaCoreLedgerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PezeshaCoreLedgerApplication.class, args);
	}

}
