package com.example.pubsub.service;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class FileNetService {

    @Retry(name = "filenetService", fallbackMethod = "fallback")
    public String uploadDocument(String partyId, String fileName) {
        System.out.println("Inside method uploadDocument: ");
        if (new Random().nextBoolean()) {
            System.out.println("Document uploaded successfully for: " + partyId + ", file: " + fileName);
//            throw new RuntimeException("FileNet upload failed");
            return "FN-" + partyId + "-" + fileName + "-" + System.currentTimeMillis();
        }
        throw new RuntimeException("FileNet upload failed");
    }

    public String fallback(String partyId, String fileName, Exception ex) {
        System.out.println("Retry failed for: " + partyId + ", file: " + fileName);
        return null;
    }
}
