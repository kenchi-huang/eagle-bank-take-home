package com.eaglebank.api.dto.User;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String password;
    private String email;
    private String address;
    private String phoneNumber;
}
