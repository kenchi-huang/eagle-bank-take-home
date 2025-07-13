package com.eaglebank.api.dto.User;

import com.eaglebank.api.model.User.Address;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String password;
    private String email;
    private Address address;
    private String phoneNumber;
}
