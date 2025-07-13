package com.eaglebank.api.service.Impl;

import com.eaglebank.api.dto.User.CreateUserRequest;
import com.eaglebank.api.dto.User.UpdateUserRequest;
import com.eaglebank.api.model.User.User;
import com.eaglebank.api.repository.UserRepository;
import com.eaglebank.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.MissingResourceException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(CreateUserRequest request) {
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setName(request.getName());
        newUser.setAddress(request.getAddress());
        newUser.setPhoneNumber(request.getPhoneNumber());
        // HASH THE PASSWORD before saving!
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(newUser);
    }

    @Override
    public User findUserById(String userId, UserDetails currentUser) {
        // First, find the user that is being requested from the database
        User requestedUser = userRepository.findById(userId)
                .orElseThrow(() -> new MissingResourceException("User not found", "User", userId));

        // THE AUTHORIZATION CHECK:
        // Compare the email from the token (currentUser.getUsername())
        // with the email of the user record from the database.
        if (!requestedUser.getEmail().equals(currentUser.getUsername())) {
            // If they don't match, the user is not authorized.
            throw new AccessDeniedException("You are not authorized to access this resource.");
        }

        // If the check passes, return the user
        return requestedUser;
    }

    @Override
    public User updateUserById(String userId, UpdateUserRequest request, UserDetails details) {
        User user = findUserById(userId, details);

        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getAddress())) {
            user.setAddress(request.getAddress());
        }
        if (StringUtils.hasText(request.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // If the check passes, return the user
        return userRepository.save(user);
    }

    @Override
    public boolean deleteUser(String userId, UserDetails details) {
        User user = findUserById(userId, details);
        userRepository.delete(user);
        return userRepository.findById(userId).isEmpty();
    }
}