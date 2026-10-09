package com.example.CAMPUSDESK.Config;

import com.example.CAMPUSDESK.Entity.User;
import com.example.CAMPUSDESK.Enums.Role;
import com.example.CAMPUSDESK.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Aprovisionamiento seguro de cuentas ADMIN y TECHNICIAN (RF-01).
 * Las credenciales se leen de variables de entorno; si no están definidas,
 * se genera una contraseña aleatoria mostrada UNA sola vez por log.
 * La inicialización por código está controlada por app.seed.enabled.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${app.seed.admin.email:admin@technova.com}")
    private String adminEmail;
    @Value("${app.seed.admin.password:}")
    private String adminPassword;

    @Value("${app.seed.tech.email:tecnico@technova.com}")
    private String techEmail;
    @Value("${app.seed.tech.password:}")
    private String techPassword;

    @Bean
    public ApplicationRunner seedUsers() {
        return args -> {
            if (!seedEnabled) {
                log.info("Seed deshabilitado (app.seed.enabled=false). Use schema_and_data.sql para datos iniciales.");
                return;
            }
            createIfMissing(adminEmail, "Administrador TechNova", adminPassword, Role.ADMIN);
            createIfMissing(techEmail, "Técnico Soporte TechNova", techPassword, Role.TECHNICIAN);
        };
    }

    private void createIfMissing(String email, String nombre, String rawPassword, Role rol) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        String password = rawPassword;
        if (password == null || password.isBlank()) {
            password = java.util.UUID.randomUUID().toString();
            log.warn("Contraseña generada para {} (rol {}): {} : cámbiela tras el primer acceso",
                    email, rol, password);
        }
        userRepository.save(User.builder()
                .nombre(nombre)
                .email(email.toLowerCase())
                .password(passwordEncoder.encode(password))
                .rol(rol)
                .build());
        log.info("Cuenta {} con rol {} creada mediante seeding seguro.", email, rol);
    }
}
