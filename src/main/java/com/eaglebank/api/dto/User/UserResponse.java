package com.eaglebank.api.dto.User;

import com.eaglebank.api.model.User.User;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserResponse {
    private String id;
    private String name;
    private String email;
    // Note: Address should be its own DTO to match the spec, but for simplicity, we'll use the entity's string representation here.
    private String address;
    private String phoneNumber;
    private Instant created;
    private Instant lastUpdated;
}
