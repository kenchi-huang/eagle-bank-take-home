package com.eaglebank.api.mapper;

import com.eaglebank.api.dto.User.UserResponse;
import com.eaglebank.api.model.User.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .address(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .created(user.getCreatedTimestamp())
                .lastUpdated(user.getUpdatedTimestamp())
                .build();
    }
}
