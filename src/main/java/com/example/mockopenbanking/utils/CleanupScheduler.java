package com.example.mockopenbanking.utils;

import ch.qos.logback.classic.Logger;
import com.example.mockopenbanking.repositories.ConsentRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;

@Configuration
@EnableScheduling
class CleanupScheduler {

    private final ConsentRepository repo;
    public CleanupScheduler(ConsentRepository repo) { this.repo = repo; }

    @Scheduled(fixedDelay = 60_000)
    public void wipeExpired() {
        int n = repo.deleteExpired(Instant.now());
        if (n > 0){
            System.out.println("Deleted {} expired consents");
            System.out.println(n);

        };
    }
}