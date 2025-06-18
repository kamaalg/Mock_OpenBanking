package com.example.mockopenbanking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class BankResponse {
    private Boolean result;
    private String message;

    }

