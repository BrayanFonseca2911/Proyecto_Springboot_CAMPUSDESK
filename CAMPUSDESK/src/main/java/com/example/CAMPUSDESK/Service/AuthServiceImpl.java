package com.example.CAMPUSDESK.Service;

import com.example.CAMPUSDESK.Dto.Request.LoginRequest;
import com.example.CAMPUSDESK.Dto.Request.RegisterRequest;
import com.example.CAMPUSDESK.Dto.Response.AuthResponse;
import com.example.CAMPUSDESK.Entity.User;
import com.example.CAMPUSDESK.Enums.Role;
import com.example.CAMPUSDESK.Exception.BadRequestException;
import com.example.CAMPUSDESK.Exception.UnauthorizedAccessException;
import com.example.CAMPUSDESK.Repository.UserRepository;
import com.example.CAMPUSDESK.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** Registro y autenticación (RF-01, RF-02). */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("El email ya está registrado");
        }
        // CP-01/CP-02: el rol SIEMPRE es USER, sin importar lo que envíe el cliente.
        User user = User.builder()
                .nombre(request.nombre().trim())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .rol(Role.USER)
                .build();
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email().trim(), request.password()));
        } catch (AuthenticationException ex) {
            // Mensaje genérico: no revelar si el email existe (RT-04).
            throw new UnauthorizedAccessException("Credenciales inválidas");
        }
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedAccessException("Credenciales inválidas"));
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        var principal = com.example.CAMPUSDESK.Security.UserPrincipal.from(user);
        String token = jwtService.generateToken(principal, Map.of(
                "uid", user.getId(),
                "rol", user.getRol().name(),
                "nombre", user.getNombre()));
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(),
                user.getId(), user.getNombre(), user.getEmail(), user.getRol());
    }
}
