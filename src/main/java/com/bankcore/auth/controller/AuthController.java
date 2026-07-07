package com.bankcore.auth.controller;

import com.bankcore.auth.dto.AuthenticatedUserResponse;
import com.bankcore.auth.dto.LoginRequest;
import com.bankcore.auth.dto.LoginResponse;
import com.bankcore.auth.dto.RefreshTokenRequest;
import com.bankcore.auth.dto.RegisterCustomerRequest;
import com.bankcore.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register-customer")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a customer user with PENDING KYC")
    public LoginResponse registerCustomer(@Valid @RequestBody RegisterCustomerRequest request) {
        return authService.registerCustomer(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and return JWT access and refresh tokens")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Issue a new access token from a refresh token")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Return the authenticated user profile")
    public AuthenticatedUserResponse me() {
        return authService.me();
    }
}
