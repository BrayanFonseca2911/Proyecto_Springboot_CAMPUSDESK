package com.example.CAMPUSDESK.Service;

import com.example.CAMPUSDESK.Dto.Response.UserResponseDTO;
import com.example.CAMPUSDESK.Enums.Role;
import com.example.CAMPUSDESK.Exception.ResourceNotFoundException;
import com.example.CAMPUSDESK.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Consultas de usuarios (solo ADMIN vía controlador/seguridad). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream().map(UserResponseDTO::from).toList();
    }

    @Override
    public List<UserResponseDTO> findAllTechnicians() {
        return userRepository.findByRol(Role.TECHNICIAN).stream().map(UserResponseDTO::from).toList();
    }

    @Override
    public UserResponseDTO findById(Long id) {
        return userRepository.findById(id)
                .map(UserResponseDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }
}
