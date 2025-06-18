package com.example.mockopenbanking.services;

import org.springframework.stereotype.Service;

@Service
public class SendOtpService {
    public boolean send_message(String phone_number){
        //DID NOT Integrate messaging since this is mock system
        return true;
    }
}
