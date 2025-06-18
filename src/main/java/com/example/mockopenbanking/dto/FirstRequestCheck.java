package com.example.mockopenbanking.dto;

import lombok.Data;

@Data

public class FirstRequestCheck {
    private String fin;
    private String phone_number;
    private String iban;
    private String balance;
    private String currency;
    private String consentId;

}
