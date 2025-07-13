package com.eaglebank.api.dto.User;

import com.eaglebank.api.model.User.Address;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private Address address;
    private String phoneNumber;
    private Instant created;
    private Instant lastUpdated;
}
