package com.example.CAMPUSDESK.Controller;

import com.example.CAMPUSDESK.Dto.Request.LoginRequest;
import com.example.CAMPUSDESK.Dto.Request.RegisterRequest;
import com.example.CAMPUSDESK.Dto.Response.AuthResponse;
import com.example.CAMPUSDESK.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Endpoints públicos de autenticación (RF-01, RF-02). */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro público y login con JWT")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registro público: el rol se fija SIEMPRE en USER (CP-01/CP-02)")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Login con email + contraseña, devuelve token JWT (CP-03)")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
