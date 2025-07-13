package com.eaglebank.api.service;

import com.eaglebank.api.dto.User.CreateUserRequest;
import com.eaglebank.api.dto.User.UpdateUserRequest;
import com.eaglebank.api.model.User.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    User createUser(CreateUserRequest request);
    User findUserById(String userId, UserDetails currentUser);
    User updateUserById(String userId, UpdateUserRequest request, UserDetails details);
    boolean deleteUser(String userId, UserDetails details);
}
