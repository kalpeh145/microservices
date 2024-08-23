package com.codedecode.microservices.VaccinationCenter.controllers;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.codedecode.microservices.VaccinationCenter.Entity.VaccinationCenter;
import com.codedecode.microservices.VaccinationCenter.Model.RequiredResponse;
import com.codedecode.microservices.VaccinationCenter.Repos.CenterRepo;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import reactor.core.publisher.Mono;
@RestController
@RequestMapping("/vaccinationcenter")
public class VaccinationCenterController {

    @Autowired
    private CenterRepo centerRepo;

    private final WebClient webClient;
    @Autowired
    public VaccinationCenterController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostMapping(path = "/add")
    public ResponseEntity<VaccinationCenter> addCitizen(@RequestBody VaccinationCenter vaccinationCenter) {
        VaccinationCenter vaccinationCenterAdded = centerRepo.save(vaccinationCenter);
        return new ResponseEntity<>(vaccinationCenterAdded, HttpStatus.OK);
    }

    @GetMapping(path = "/id/{id}")
    @CircuitBreaker(name = "getAllDataCircuitBreaker", fallbackMethod = "getAllDataFallback")
    public Mono<ResponseEntity<RequiredResponse>> getAllDataBasedOnCenterId(@PathVariable Integer id) {
        return Mono.fromCallable(() -> {
            RequiredResponse requiredResponse = new RequiredResponse();

            // Get vaccination center detail
            VaccinationCenter center = centerRepo.findById(id).orElseThrow(() -> new RuntimeException("Center not found"));
            requiredResponse.setCenter(center);

            return requiredResponse;
        })
        .flatMap(requiredResponse -> 
            webClient.get()
                     .uri("lb://CITIZEN-SERVICE/citizen/id/" + id) // Use service name for load balancing
                     .retrieve()
                     .bodyToMono(List.class)
                     .map(citizens -> {
                         requiredResponse.setCitizens(citizens);
                         return ResponseEntity.ok(requiredResponse);
                     })
                     .onErrorResume(e -> {
                         requiredResponse.setCitizens(Collections.emptyList());
                         return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(requiredResponse));
                     })
        );
    }
    
   

    // Fallback method
    public Mono<ResponseEntity<RequiredResponse>> getAllDataFallback(Integer id, Throwable throwable) {
        RequiredResponse requiredResponse = new RequiredResponse();
        VaccinationCenter center = centerRepo.findById(id).orElse(null);
        requiredResponse.setCenter(center);
        requiredResponse.setCitizens(Collections.emptyList());
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(requiredResponse));
    }
}
