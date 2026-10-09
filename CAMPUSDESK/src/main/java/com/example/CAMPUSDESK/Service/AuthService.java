package com.example.CAMPUSDESK.Service;

import com.example.CAMPUSDESK.Dto.Request.LoginRequest;
import com.example.CAMPUSDESK.Dto.Request.RegisterRequest;
import com.example.CAMPUSDESK.Dto.Response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
