package com.eaglebank.api.dto.Auth;
import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
