package com.example.mockopenbanking.services;

import com.example.mockopenbanking.repositories.ConsentRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class RejectionService {
    private final ConsentRepository repo;


    public RejectionService(ConsentRepository repo) {
        this.repo = repo;
    }
    public void startSignatureWindow(String consentId) {
        repo.markPendingSignature(
                consentId,
                Instant.now().plus(Duration.ofMinutes(30))
        );
    }

    public void acceptSignature(String consentId) {
        repo.markSigned(consentId);
    }
    public void reject(String consentId){
        repo.markRejected(consentId, Instant.now().plus(Duration.ofMinutes(30)));
    }
}
