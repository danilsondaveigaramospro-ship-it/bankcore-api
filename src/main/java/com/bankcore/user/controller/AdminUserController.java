package com.bankcore.user.controller;

import com.bankcore.user.domain.User;
import com.bankcore.user.dto.CreateEmployeeRequest;
import com.bankcore.user.dto.UserResponse;
import com.bankcore.user.service.UserAdministrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserAdministrationService userAdministrationService;

    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a bank employee user")
    public UserResponse createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        return toResponse(userAdministrationService.createEmployee(request));
    }

    @PatchMapping("/users/{userId}/disable")
    @Operation(summary = "Disable a user")
    public UserResponse disableUser(@PathVariable UUID userId) {
        return toResponse(userAdministrationService.disableUser(userId));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }
}
