package com.example.mockopenbanking.dto;

import jakarta.persistence.Id;
import lombok.Data;

@Data
public class LoginRequest {
    @Id
    private String Fin;

    private String phone_number;
}
