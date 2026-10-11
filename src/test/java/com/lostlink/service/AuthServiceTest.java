package com.lostlink.service;

import com.lostlink.dto.request.RegisterRequest;
import com.lostlink.dto.response.AuthResponse;
import com.lostlink.entity.User;
import com.lostlink.exception.BadRequestException;
import com.lostlink.repository.UserRepository;
import com.lostlink.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("Test Student")
                .email("student@college.edu")
                .password("plainSecret123")
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail("student@college.edu")).thenReturn(false);
        when(passwordEncoder.encode("plainSecret123")).thenReturn("$2a$10$hashedpasswordstring");

        User savedUser = User.builder()
                .id(10L)
                .name("Test Student")
                .email("student@college.edu")
                .password("$2a$10$hashedpasswordstring")
                .active(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("Test Student", response.getUser().getName());
        assertEquals("student@college.edu", response.getUser().getEmail());

        // Verify password hashing was called
        verify(passwordEncoder, times(1)).encode("plainSecret123");
    }

    @Test
    void register_DuplicateEmail_ThrowsBadRequestException() {
        when(userRepository.existsByEmail("student@college.edu")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail("student@college.edu")).thenReturn(java.util.Optional.of(
                User.builder()
                        .id(10L)
                        .name("Test Student")
                        .email("student@college.edu")
                        .password("$2a$10$hashedpasswordstring")
                        .active(true)
                        .build()
        ));
        when(passwordEncoder.matches("plainSecret123", "$2a$10$hashedpasswordstring")).thenReturn(true);

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("mock.jwt.token");

        com.lostlink.dto.request.LoginRequest loginRequest = com.lostlink.dto.request.LoginRequest.builder()
                .email("student@college.edu")
                .password("plainSecret123")
                .build();

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("student@college.edu", response.getUser().getEmail());
    }

    @Test
    void login_UnregisteredEmail_ThrowsBadRequestException() {
        when(userRepository.findByEmail("unknown@college.edu")).thenReturn(java.util.Optional.empty());

        com.lostlink.dto.request.LoginRequest loginRequest = com.lostlink.dto.request.LoginRequest.builder()
                .email("unknown@college.edu")
                .password("plainSecret123")
                .build();

        assertThrows(BadRequestException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_WrongPassword_ThrowsBadRequestException() {
        when(userRepository.findByEmail("student@college.edu")).thenReturn(java.util.Optional.of(
                User.builder()
                        .id(10L)
                        .name("Test Student")
                        .email("student@college.edu")
                        .password("$2a$10$hashedpasswordstring")
                        .active(true)
                        .build()
        ));
        when(passwordEncoder.matches("wrongSecret", "$2a$10$hashedpasswordstring")).thenReturn(false);

        com.lostlink.dto.request.LoginRequest loginRequest = com.lostlink.dto.request.LoginRequest.builder()
                .email("student@college.edu")
                .password("wrongSecret")
                .build();

        assertThrows(BadRequestException.class, () -> authService.login(loginRequest));
    }
}
