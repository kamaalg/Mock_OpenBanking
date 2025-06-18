package com.example.mockopenbanking;

import com.example.mockopenbanking.dto.DocsResponse;
import com.example.mockopenbanking.repositories.FinRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.print.Doc;

@SpringBootApplication
public class MockOpenBankingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MockOpenBankingApplication.class, args);
	}
	@Bean
	CommandLineRunner seedFinTable(FinRepository finRepo) {
		return args -> {
			// avoid duplicates if you restart with a file‑based DB
			if (finRepo.existsById("FIN123456")) return;
			DocsResponse sample1 = new DocsResponse();
			sample1.setFin("FIN123456");
			sample1.setIBAN("AZ50BANK0000000001234567");
			sample1.setBalance("123000.00");
			DocsResponse sample2 = new DocsResponse();
			sample2.setFin("FIN54321");
			sample2.setBalance("23.00");
			sample2.setIBAN("PRE345");
			finRepo.save(sample1);
			finRepo.save(sample2);
		};
	}

}
