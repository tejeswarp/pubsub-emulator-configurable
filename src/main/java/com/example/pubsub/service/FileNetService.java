package com.example.pubsub.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class FileNetService {

    // Outer layer with Retry
    @Retry(name = "filenetRetry", fallbackMethod = "retryFallback")
    public String uploadDocument(String partyId, String fileName) {
        return uploadWithCircuitBreaker(partyId, fileName);
    }

    // Inner layer with CircuitBreaker
    @CircuitBreaker(name = "filenetCB", fallbackMethod = "cbFallback")
    public String uploadWithCircuitBreaker(String partyId, String fileName) {
        System.out.println("Calling FileNet: " + partyId + ", " + fileName);

        if (new Random().nextBoolean()) {
            System.out.println("File uploaded for: " + partyId);
            throw new RuntimeException("Simulated FileNet failure");
//            return "FN-" + partyId + "-" + fileName + "-" + System.currentTimeMillis();
        }

        System.out.println("Simulated failure for: " + partyId);
        throw new RuntimeException("Simulated FileNet failure");
    }

    // Retry fallback
    public String retryFallback(String partyId, String fileName, Throwable ex) {
        System.out.println("Retry fallback triggered for: " + partyId + " due to: " + ex.getMessage());
        return null;
    }

    // Circuit breaker fallback
    public String cbFallback(String partyId, String fileName, Throwable ex) {
        System.out.println("⚡ CircuitBreaker fallback: Circuit open or 100% failure rate. " + ex.getMessage());
        return null;
    }
}
