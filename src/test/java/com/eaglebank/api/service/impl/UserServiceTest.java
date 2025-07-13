package com.eaglebank.api.service.impl;

import com.eaglebank.api.dto.User.CreateUserRequest;
import com.eaglebank.api.dto.User.UpdateUserRequest;
import com.eaglebank.api.model.User.Address;
import com.eaglebank.api.model.User.User;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.UserRepository;
import com.eaglebank.api.service.Impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.MissingResourceException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDetails currentUserDetails;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");

        // Mock UserDetails, which is what Spring Security will provide
        currentUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username("test@example.com")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    void createUser_shouldSaveAndReturnUser() {
        // Arrange
        Address address = Address.builder()
                .line1("123 Fake Street")
                .city("Springfield")
                .postcode("12345")
                .country("United States of America")
                .build();
        var request = new CreateUserRequest();
        request.setName("New User");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setAddress(address);
        request.setPhoneNumber("555-0100");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        // We need to use an ArgumentCaptor to verify the User object passed to save()
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User createdUser = userService.createUser(request);

        // Assert
        assertNotNull(createdUser);
        assertEquals("New User", createdUser.getName());
        assertEquals("new@example.com", createdUser.getEmail());
        assertEquals("encodedPassword", createdUser.getPassword());
        assertEquals(address, createdUser.getAddress());
        assertEquals("555-0100", createdUser.getPhoneNumber());

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findUserById_shouldReturnUser_whenUserIsAuthorized() {
        // Arrange
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

        // Act
        User foundUser = userService.findUserById("user-123", currentUserDetails);

        // Assert
        assertNotNull(foundUser);
        assertEquals("user-123", foundUser.getId());
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void findUserById_shouldThrowAccessDenied_whenUserIsNotAuthorized() {
        // Arrange
        UserDetails otherUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username("other@example.com")
                .password("password")
                .roles("USER")
                .build();
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            userService.findUserById("user-123", otherUserDetails);
        });
    }

    @Test
    void findUserById_shouldThrowMissingResourceException_whenUserNotFound() {
        // Arrange
        when(userRepository.findById("user-404")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MissingResourceException.class, () -> {
            userService.findUserById("user-404", currentUserDetails);
        });
    }

    @Test
    void updateUserById_shouldUpdateFieldsCorrectly() {
        // Arrange
        var request = new UpdateUserRequest();
        request.setName("Updated Name");
        request.setPassword("newPassword");
        // Email, address, phone are null/empty and should not be updated

        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User updatedUser = userService.updateUserById("user-123", request, currentUserDetails);

        // Assert
        assertNotNull(updatedUser);
        assertEquals("Updated Name", updatedUser.getName()); // Updated
        assertEquals("newHashedPassword", updatedUser.getPassword()); // Updated
        assertEquals("test@example.com", updatedUser.getEmail()); // Unchanged

        verify(userRepository).save(testUser);
        verify(passwordEncoder).encode("newPassword");
    }

    @Test
    void deleteUser_shouldSucceed_whenUserIsAuthorized() {
        // Arrange
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser)).thenReturn(Optional.empty());
        doNothing().when(userRepository).delete(testUser);

        // Act
        boolean isDeleted = userService.deleteUser("user-123", currentUserDetails);

        // Assert
        assertTrue(isDeleted);
        verify(userRepository).delete(testUser);
        verify(userRepository, times(2)).findById("user-123");
    }
}