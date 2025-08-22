package com.maisprati.hub.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {
	static {
		Dotenv dotenv = Dotenv.load();
		// define as variáveis como properties do sistema
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
	}
}
