package com.eaglebank.api.controller;


import com.eaglebank.api.dto.User.CreateUserRequest;
import com.eaglebank.api.dto.User.UpdateUserRequest;
import com.eaglebank.api.dto.User.UserResponse;
import com.eaglebank.api.mapper.UserMapper;
import com.eaglebank.api.model.User.User;
import com.eaglebank.api.security.JwtService;
import com.eaglebank.api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.MissingResourceException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("usr-123");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setCreatedTimestamp(Instant.now());
        testUser.setUpdatedTimestamp(Instant.now());

        // Mock the mapper to return a DTO representation of our testUser
        when(userMapper.toResponse(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return UserResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .address(user.getAddress())
                    .phoneNumber(user.getPhoneNumber())
                    .created(user.getCreatedTimestamp())
                    .lastUpdated(user.getUpdatedTimestamp())
                    .build();
        });
    }

    @Test
    void createUser_shouldReturn201AndUserResponse() throws Exception {
        // Arrange
        var request = new CreateUserRequest();
        request.setName("New User");
        request.setEmail("new@example.com");
        request.setPassword("password");

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/v1/users")
                        .with(csrf()) // Add CSRF token for POST requests
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("usr-123"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getUser_shouldReturn200AndUserResponse_whenAuthorized() throws Exception {
        // Arrange
        when(userService.findUserById(eq("usr-123"), any())).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/v1/users/usr-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usr-123"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "attacker@example.com")
    void getUser_shouldReturn403Forbidden_whenNotAuthorized() throws Exception {
        // Arrange
        when(userService.findUserById(eq("usr-123"), any()))
                .thenThrow(new AccessDeniedException("You are not authorized to access this resource."));

        // Act & Assert
        mockMvc.perform(get("/v1/users/usr-123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getUser_shouldReturn404NotFound_whenUserDoesNotExist() throws Exception {
        // Arrange
        when(userService.findUserById(eq("usr-404"), any()))
                .thenThrow(new MissingResourceException("User not found", "User", "usr-404"));

        // Act & Assert
        mockMvc.perform(get("/v1/users/usr-404"))
                .andExpect(status().isNotFound()); // Assuming you have a @ControllerAdvice to handle this
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateUser_shouldReturn200AndUpdatedUser() throws Exception {
        // Arrange
        var request = new UpdateUserRequest();
        request.setName("Updated Name");

        User updatedUser = new User();
        updatedUser.setId("usr-123");
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("test@example.com");

        when(userService.updateUserById(eq("usr-123"), any(UpdateUserRequest.class), any())).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(patch("/v1/users/usr-123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteUser_shouldReturn204NoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/v1/users/usr-123").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
