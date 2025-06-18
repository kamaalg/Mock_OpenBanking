package com.example.mockopenbanking.dto;

import lombok.Data;

import java.time.Duration;
@Data
public class FinalApprovalResponse {
    private String IBAN;
    private String ConsentId;
    private Duration period;
}
