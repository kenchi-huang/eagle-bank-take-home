package com.eaglebank.api.dto.User;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String name;
    private String password;
    private String email;
    private String address;
    private String phoneNumber;
}
