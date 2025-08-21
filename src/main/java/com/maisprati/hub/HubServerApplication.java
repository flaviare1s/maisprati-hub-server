package com.maisprati.hub;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class HubServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HubServerApplication.class, args);
	}

	@Bean
	public CommandLineRunner testMongo(MongoTemplate mongoTemplate) {
		return args -> {
			// Verifica se a conexão está funcionando
			mongoTemplate.getDb().getName();
			System.out.println("✅ Conexão com MongoDB estabelecida! Banco: " + mongoTemplate.getDb().getName());
		};
	}
}
