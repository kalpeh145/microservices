package com.codedecode.microservices.VaccinationCenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class VaccinationCenterApplication {

	public static void main(String[] args) {
		SpringApplication.run(VaccinationCenterApplication.class, args);
	}
	
	@Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

}
