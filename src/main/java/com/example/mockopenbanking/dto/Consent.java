package com.example.mockopenbanking.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "consent")
public class Consent {
    @Id
    private String id;
    private String status;
    private Instant expiresAt;
    public String phone_number;
    public String IBAN;
}