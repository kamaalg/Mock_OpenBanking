package com.example.mockopenbanking.dto;

import java.time.Duration;

public class SignatureRequest {
    private String Fin;
    private String IBAN;
    private String balance;
    private String terms_and_conditions;
    private Duration period;
}
