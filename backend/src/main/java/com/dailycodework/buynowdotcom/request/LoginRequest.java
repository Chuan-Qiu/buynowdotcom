package com.dailycodework.buynowdotcom.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}