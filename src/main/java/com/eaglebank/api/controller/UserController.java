package com.eaglebank.api.controller;

import com.eaglebank.api.dto.User.CreateUserRequest;
import com.eaglebank.api.dto.User.UpdateUserRequest;
import com.eaglebank.api.dto.User.UserResponse;
import com.eaglebank.api.mapper.UserMapper;
import com.eaglebank.api.model.User.User;
import com.eaglebank.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest createUserRequest) {
        User user = userService.createUser(createUserRequest);
        return new ResponseEntity<>(userMapper.toResponse(user), HttpStatus.CREATED);
    }

    @GetMapping("{userId}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable String userId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        User user = userService.findUserById(userId, currentUser);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PatchMapping("{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String userId,
            @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        User user = userService.updateUserById(userId, request, currentUser);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @DeleteMapping("{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable String userId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        userService.deleteUser(userId, currentUser);
        return ResponseEntity.noContent().build();
    }

}
