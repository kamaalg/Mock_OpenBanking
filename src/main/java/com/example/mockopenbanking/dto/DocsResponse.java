package com.example.mockopenbanking.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.Duration;

@Data
@Entity
public class DocsResponse {
    @Id
    private String Fin;
    private String IBAN;
    private String balance;
    private String terms_and_conditions;
    private Duration period;

}
