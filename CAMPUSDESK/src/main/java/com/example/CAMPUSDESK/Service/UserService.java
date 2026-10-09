package com.example.CAMPUSDESK.Service;

import com.example.CAMPUSDESK.Dto.Response.UserResponseDTO;

import java.util.List;

public interface UserService {
    List<UserResponseDTO> findAll();
    List<UserResponseDTO> findAllTechnicians();
    UserResponseDTO findById(Long id);
}
