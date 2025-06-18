package com.example.mockopenbanking.repositories;

import com.example.mockopenbanking.dto.Consent;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;


@Repository
public interface ConsentRepository extends JpaRepository<Consent, String> {
    @Modifying
    @Transactional
    @Query("""
        update Consent c
           set c.status    = 'PENDING_SIGNATURE',
               c.expiresAt = :expiry
         where c.id = :id
           and c.status    = 'ACTIVE'          \s
    """)
    void markPendingSignature(String id, Instant expiry);

    @Modifying
    @Transactional
    @Query("""
        update Consent c
           set c.status = 'SIGNED'
         where c.id = :id
           and c.status = 'PENDING_SIGNATURE'\s
    """)
    void markSigned(String id);
    @Modifying
    @Transactional
    @Query("""
        update Consent c
           set c.status = 'REJECTED',
               c.expiresAt = :expiry
         where c.id = :id
    """)
    void markRejected(String id, Instant expiry);

    @Modifying
    @Transactional
    @Query("""
        delete from Consent c
         where c.status = 'REJECTED'
           and c.expiresAt <= :now
    """)
    int deleteExpired(Instant now);
}
