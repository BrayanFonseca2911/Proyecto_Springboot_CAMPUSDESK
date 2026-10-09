package com.example.CAMPUSDESK.Controller;

import com.example.CAMPUSDESK.Dto.Response.UserResponseDTO;
import com.example.CAMPUSDESK.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Consultas de usuarios: exclusivas de ADMIN (RF-01). */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Gestión de usuarios y técnicos (solo ADMIN)")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Listar todos los usuarios registrados")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @Operation(summary = "Listar técnicos disponibles para asignación")
    @GetMapping("/technicians")
    public ResponseEntity<List<UserResponseDTO>> findTechnicians() {
        return ResponseEntity.ok(userService.findAllTechnicians());
    }

    @Operation(summary = "Obtener un usuario por id")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
